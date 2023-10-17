package com.wangym.lombok.job.impl;

public class SystemOutPrintExample {
    @JsonProperty(value = "open_id")
    private String name;//这是一个单行注释2


    public void test() {
        System.out.println("hello world");
    }
    public static void main(String[] args) {
        
    }

}
