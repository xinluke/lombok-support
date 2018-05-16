package com.wangym.lombok.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author wangym
 * @version 创建时间：2018年5月14日 上午10:30:27
 */
@Component
@Slf4j
public class ReplaceLoggerJob {

    public void handle(File file) throws IOException {
        // 以utf-8的编码方式读取文件
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"))) {
            br.mark((int) (file.length() + 1));
            String line;
            String className = null;
            String classLine = null;
            String loggerName = null;
            String loggerLine = null;
            boolean annotationExist = false;
            while ((line = br.readLine()) != null) {
                className = getClassName(line);
                if (className != null) {
                    classLine = line;
                    log.info("class name:{}", className);
                    break;
                }
            }
            br.reset();
            while ((line = br.readLine()) != null) {
                loggerName = getLoggerVal(line, className);
                if (loggerName != null) {
                    loggerLine = line;
                    log.info("logger name:{}", loggerName);
                    break;
                }
            }
            br.reset();
            while ((line = br.readLine()) != null) {
                if (line.contains("@Slf4j")) {
                    annotationExist = true;
                    break;
                }
            }
            // 不符合条件立刻终止
            if (className == null || loggerName == null) {
                return;
            }
            StringBuffer newBody = new StringBuffer();
            log.info("当前文件符合转换,class name:{},logger name:{}", className, loggerName);
            String target = loggerName + ".";
            // 重置至mark点
            br.reset();
            while ((line = br.readLine()) != null) {
                if (line.contains("import org.slf4j.LoggerFactory;")) {
                    if (annotationExist) {
                        continue;
                    }
                    newBody.append("import lombok.extern.slf4j.Slf4j;");
                } else if (line.contains(classLine)) {
                    if (!annotationExist) {
                        newBody.append("@Slf4j\n");
                    }
                    newBody.append(classLine);
                } else if (line.contains(loggerLine)) {
                    continue;
                } else if (line.contains("import org.slf4j.Logger;")) {
                    continue;
                } else {
                    newBody.append(line.replace(target, "log."));
                }
                newBody.append("\n");

            }
            // 以utf-8编码的方式写入文件中
            FileCopyUtils.copy(newBody.toString().getBytes("utf-8"), file);
            // System.out.println(newBody.toString());
        }
    }

    private String getLoggerVal(String line, String className) {
        if (className == null) {
            return null;
        }
        if (testExistLogger(line, className)) {
            String leftText = line.split("=")[0];
            String[] arr = leftText.split("\\s+");
            // 取最后一个单词即是logger变量的名字
            return arr[arr.length - 1];
        } else {
            return null;
        }
    }

    private boolean testExistLogger(String line, String className) {
        Pattern p = Pattern.compile("=\\s*LoggerFactory.getLogger\\(" + className + "\\.class(\\.getName\\(\\))*\\)");
        Matcher matcher = p.matcher(line);
        return matcher.find();
    }

    private String getClassName(String line) {
        if (testExistClassName(line)) {
            String[] arr = line.split("\\s+");
            // 第三个单词是类名
            String className = arr[2];
            if (className.endsWith("{")) {
                className = className.substring(0, className.length() - 1);
            }
            return className;
        } else {
            return null;
        }
    }

    private boolean testExistClassName(String line) {
        Pattern p = Pattern.compile("public\\s+(class|enum)\\s+\\w.*");
        Matcher matcher = p.matcher(line);
        return matcher.find();
    }
}
