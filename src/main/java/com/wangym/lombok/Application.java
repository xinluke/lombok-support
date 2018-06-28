package com.wangym.lombok;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

@SpringBootApplication
@Slf4j
public class Application {

    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);
        // 设置javaparse输出文本的换行符
        System.setProperty("line.separator", "\n");
        JobController ctrl = ctx.getBean(JobController.class);
        ctrl.start();
    }
}
