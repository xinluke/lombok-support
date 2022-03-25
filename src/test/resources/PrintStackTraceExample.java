package com.wangym.lombok.job.impl;
import lombok.extern.slf4j.*;
public class PrintStackTraceExample {

    public void test() {
        try {
            int a = 10;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
