package com.wangym.lombok.job;

import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author wangym
 * @version 创建时间：2018年6月25日 下午1:40:20
 */
//@Component
@Slf4j
public class MavenDependencyVersionReplaceJob extends AbstractJob {

    public MavenDependencyVersionReplaceJob() {
        super("pom.xml");
    }

    @Override
    public void exec(File file) {
        try {
            doHandle(file);
        } catch (Exception e) {
            log.info("处理文件失败：{}", file.getPath(), e);
        }
    }

    private void doHandle(File file) throws IOException, DocumentException {
        prepareFormat(file);
        SAXReader reader = new SAXReader();
        Document document = reader.read(file);
        // 获取文档根节点
        Element root = document.getRootElement();
        // 查询properties节点，如果没有就add一个
        Element propertiesElement = root.element("properties");
        if (propertiesElement == null) {
            propertiesElement = root.addElement("properties");
        }
        checkProperties(document);
        Element contactElem = root.element("dependencies");
        Map<String, String> col = new HashMap<>();
        // 获得指定节点下面的子节点
        if (contactElem != null) {
            List<Element> contactList = contactElem.elements("dependency");
            for (Element e : contactList) {
                Element versionEle = e.element("version");
                if (versionEle == null) {
                    continue;
                }
                String version = versionEle.getStringValue().toString();
                if (version.contains("$")) {
                    continue;
                }
                String artifactId = e.element("artifactId").getStringValue();
                String newVersionName = artifactId + ".version";
                col.put(newVersionName, version);
                Element e1 = propertiesElement.addElement(newVersionName);
                e1.setText(version);
                String formatVersionName = String.format("${%s}", newVersionName);
                versionEle.setText(formatVersionName);
            }
        }
        if (!col.isEmpty()) {
            try {
                OutputFormat format = new OutputFormat();
                format.setPadText(true);
                format.setEncoding("UTF-8");
                // 设置换行
                format.setNewlines(true);
                // 生成缩进
                format.setIndent(true);
                // 使用4个空格进行缩进, 可以兼容文本编辑器
                format.setIndent("    ");
                format.setNewLineAfterDeclaration(false);
                /** 将document中的内容写入文件中 */
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
                XMLWriter writer = new XMLWriter(bw, format);
                writer.write(document);
                writer.close();
                // 使用format.setTrimText(true);去删除空行，会导致文件的全部的换行符都会被去除，文件样式大变
                prettyPrint(file);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    private Map<String, String> checkProperties(Document document) {
        String text = document.asXML();
        // 获取文档根节点
        Element root = document.getRootElement();
        // 获取某个节点
        Element propertiesElement = root.element("properties");
        // 获取该节点下所有的元素
        List<Element> els = propertiesElement.elements();
        Map<String, String> proMap = new HashMap<String, String>();
        Iterator<Element> it = els.iterator();
        while (it.hasNext()) {
            Element e = it.next();
            String name = e.getName();
            String formatVersionName = String.format("${%s}", name);
            if (!text.contains(formatVersionName)) {
                // 去除无效的引用
                it.remove();
                continue;
            }
            String value = e.getStringValue();
            proMap.put(name, value);
        }
        return proMap;
    }

    private void prettyPrint(File file) throws UnsupportedEncodingException, IOException {
        byte[] bytes = FileCopyUtils.copyToByteArray(file);
        String newBody = new String(bytes, "utf-8").replaceAll(" +\n", "");
        // 修正为原来的格式
        // newBody = newBody.replaceAll("</project>", "</project>\n");
        FileCopyUtils.copy(newBody.getBytes("utf-8"), file);
    }

    private void prepareFormat(File file) throws UnsupportedEncodingException, IOException {
        byte[] bytes = FileCopyUtils.copyToByteArray(file);
        String newBody = new String(bytes, "utf-8").replaceAll("> +", ">");
        if (!newBody.contains("<properties>")) {
            // 因为dom4j插properties节点到指定的位置不好处理
            newBody = newBody.replaceAll("<dependencies>", "<properties></properties><dependencies>");
        }
        // 如果是空行，删除空白字符
        newBody = newBody.replaceAll(" +\n", "\n");
        FileCopyUtils.copy(newBody.getBytes("utf-8"), file);
    }
}
