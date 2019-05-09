package com.wangym.lombok.job.impl;

import com.wangym.lombok.job.AbstractJob;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.jdom.MavenJDOMWriter;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jdom2.JDOMException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * @author wangym
 * @version 创建时间：2018年6月25日 下午1:40:20
 */
@ConditionalOnProperty(value = "maven.trim.enable", havingValue = "true")
@Component
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

    private void doHandle(File file) throws FileNotFoundException, IOException, XmlPullParserException, JDOMException {
        // Reading
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new FileInputStream(file));
        // Editing
        ModelWrapper modelWrapper = new ModelWrapper(model);
        modelWrapper.process();
        if (modelWrapper.isHasModify()) {
            // Writing
            new MavenJDOMWriter(model)
                    .setExpandEmptyElements(false)// pom.xml需要简化配置，所以override原本的配置，设置为自闭合
                    .write(model, file);
        }
    }

    @Getter
    class ModelWrapper {
        private Model model;
        private boolean hasModify = false;

        public ModelWrapper(Model model) {
            super();
            this.model = model;
        }

        public void process() {
            List<Dependency> dep = model.getDependencies();
            for (Dependency d : dep) {
                String a = d.getArtifactId();
                String v = d.getVersion();
                // 如果是原始的值类型的版本号才进行处理
                if (!isVersion(v)) {
                    continue;
                }
                insertProperty(a, v);
                d.setVersion(getNewVersion(a));
                // 说明pom.xml有变动
                hasModify = true;
            }
        }

        private void insertProperty(String artifactId, String version) {
            String key = artifactId + ".version";
            Properties prop = model.getProperties();
            prop.setProperty(key, version);
        }

        private String getNewVersion(String artifactId) {
            String key = artifactId + ".version";
            return "${" + key + "}";
        }

        private boolean isVersion(String version) {
            if (StringUtils.isEmpty(version)) {
                return false;
            }
            if (version.contains("$")) {
                return false;
            }
            return true;
        }

    }


}
