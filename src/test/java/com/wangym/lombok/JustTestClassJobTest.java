package com.wangym.lombok;

import com.wangym.lombok.job.impl.JustTestClassJob;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

/**
 * @author wangym
 * @version 创建时间：2018年12月27日 上午11:03:54
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class JustTestClassJobTest {
    @Autowired
    private JustTestClassJob job;
    @Test
    public void handle() throws IOException {
        job.exec(new ClassPathResource("DateUtil.java").getFile());
    }


}
