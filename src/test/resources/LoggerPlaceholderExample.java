package com.wangym.lombok.job.impl;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LoggerPlaceholderExample {

    public void case1() {
        log.info(String.format("tmp=%s", "123"));

    }
    public void case2() {
        log.error("数据写ES失败, 内容: {}", JSON.toJSONString(new Object()));

    }
    public void case3() {
        log.info("hello" + " " + "world");

    }
    public void case4(Exception e) {
        log.error("", e);
    }

}
