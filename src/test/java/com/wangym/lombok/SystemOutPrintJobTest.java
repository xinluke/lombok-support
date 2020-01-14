package com.wangym.lombok;

import com.wangym.lombok.job.impl.SystemOutPrintJob;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

/**
 * @author wangym
 * @version 创建时间：2018年11月6日 下午2:48:11
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class SystemOutPrintJobTest {
    @Autowired
    private SystemOutPrintJob job;

    @Test
    public void handle() throws IOException {
        job.handle(new ClassPathResource("SystemOutPrintExample.java").getFile());
    }
}
