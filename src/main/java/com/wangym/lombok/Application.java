package com.wangym.lombok;

import com.wangym.lombok.job.ReplaceLoggerJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;

@SpringBootApplication
@Slf4j
public class Application {

    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);
        ReplaceLoggerJob modifyJob = ctx.getBean(ReplaceLoggerJob.class);
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        // 设置javaparse输出文本的换行符
        System.setProperty("line.separator", "\n");
        // 以当前jar为基点，搜索下面全部的文件
        Resource[] resources = resolver.getResources("file:./**");
        for (Resource resource : resources) {
            if (!resource.getFilename().endsWith(".java")) {
                continue;
            }
            String uriStr = resource.getURI().toString();
            log.info("尝试读取文件：{}", uriStr);
            try {
                modifyJob.handle(resource.getFile());
            } catch (Exception e) {
                log.info("读取文件失败：{}", uriStr, e);
            }
        }
    }
}
