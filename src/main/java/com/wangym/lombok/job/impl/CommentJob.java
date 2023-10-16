package com.wangym.lombok.job.impl;

import com.wangym.lombok.job.AbstractJob;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CommentJob extends AbstractJob {

    public CommentJob() {
        super(".java");
    }

    @Override
    public void exec(File file) throws IOException {
        //按行读取文件
        StringBuffer sb = new StringBuffer();
        boolean changeFlag = false;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                //判断是否包含行尾注释，将行尾注释提到上一行
                changeFlag = true && processLine(line, sb);
            }
        }
        //如果文件内容发生变化，重新写入文件
        if (changeFlag) {
            FileCopyUtils.copy(sb.toString().getBytes("utf-8"), file);
        }
    }

    private boolean processLine(String text, StringBuffer sb) {
        Pattern pattern = Pattern.compile("^(\\s*)(.*?;)\\s*(//.*)$");  // 匹配行尾注释
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            String commentAlignment = matcher.group(1);  // 获取注释对齐部分
            String code = matcher.group(2);  // 获取代码部分
            String comment = matcher.group(3);  // 获取行尾注释

            sb.append(commentAlignment + comment + "\n");
            sb.append(commentAlignment + code + "\n");
            return true;
        } else {
            sb.append(text + "\n");
            return false;
        }
    }


}
