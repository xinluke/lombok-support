package com.wangym.lombok.job.impl;

import org.slf4j.LoggerFactory;

public class LoggerExample {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public void test() {
        logger.info("hello world");
    }

}
