package com.wangym.lombok.job.impl;

import org.slf4j.LoggerFactory;

public class LoggerExample {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public void test() {
        int a = 5;
        double b = 6d;
        double b1 = 6.1;
        long c = 7;
        long c2 = 8l;
        if (null != new Object()) {
            logger.info("hello world");
        }
    }

}
