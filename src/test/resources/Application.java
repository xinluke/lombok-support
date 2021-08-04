package com.wangym.lombok;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
@ImportResource("classpath:spring/spring-all.xml")
public class Application {

    public static void main(String[] args) {
        List<String> list1=new ArrayList<>();
        List<String> list=Lists.newArrayList();
        List<String> list2=Lists.newArrayList("abc","cdf");
        Map<String,String> map =Maps.newHashMap();
        SpringApplication app = new SpringApplication(Application.class);
        app.run(args);
    }

    public synchronized static void test1(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        app.run(args);
    }
}

