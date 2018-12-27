package com.wangym.lombok;

import com.wangym.lombok.job.NoArgsConstructorJob;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;

/**
* @author  wangym
* @version 创建时间：2018年12月27日 上午11:03:54
*/
@RunWith(SpringRunner.class)
@SpringBootTest
public class NoArgsConstructorJobTest {
    @Autowired
    private NoArgsConstructorJob job;

    @Test
    public void handle() throws IOException {
        job.handle(new File("c:/ProfileTrendQueryParam.java"));
    }
}
