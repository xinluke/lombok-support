package com.wangym.lombok;

import com.wangym.lombok.job.ReplaceLoggerJob;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {

    @Autowired
    private ReplaceLoggerJob job;

    @Test
    public void contextLoads() throws IOException {
        job.handle(new File("c:AppGroupController.java"));
    }

}
