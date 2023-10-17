package com.wangym.lombok.job.impl;

import com.wangym.lombok.job.AbstractJob;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CommentJob extends AbstractJob {

    public CommentJob() {
        super(".java");
    }

    @Override
    public void exec(File file) throws IOException {
        //按行读取文件
        List<String> stringList = new ArrayList<>();
        boolean changeFlag = false;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                //判断是否包含行尾注释，将行尾注释提到上一行
                if (processLine(line, stringList)) {
                    changeFlag = true;
                }
            }
        }
        //如果文件内容发生变化，重新写入文件
        if (changeFlag) {
            StringBuffer sb = new StringBuffer();
            for (String s : stringList) {
                sb.append(s).append("\n");
            }
            FileCopyUtils.copy(sb.toString().getBytes("utf-8"), file);
        }
    }

    private boolean processLine(String text, List<String> lines) {
        Pattern pattern = Pattern.compile("^(\\s*)(.*?;)\\s*(//.*)$");  // 匹配行尾注释
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            String commentAlignment = matcher.group(1);  // 获取注释对齐部分
            String code = matcher.group(2);  // 获取代码部分
            String comment = matcher.group(3);  // 获取行尾注释

            addToHead(lines, commentAlignment + comment);
            lines.add(commentAlignment + code);
            return true;
        } else {
            lines.add(text);
            return false;
        }
    }

    private void addToHead(List<String> lines, String text) {
        //for倒序循环遍历，找到第一个不是注解的行
        int index = 0;
        for (int i = lines.size() - 1; i >= 0; i--) {
            if (!isAnnotaion(lines.get(i))) {
                break;
            } else {
                //记录最后一个注解的位置
                index = i;
            }

        }
        if (index != 0) {
            lines.add(index, text);
        } else {
            lines.add(text);
        }
    }

    private boolean isAnnotaion(String text) {
        //判断是否是注解
        Pattern pattern = Pattern.compile("^\\s*@\\D*$");
        Matcher matcher = pattern.matcher(text);
        return matcher.find();
    }


}
