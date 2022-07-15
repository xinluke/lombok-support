package com.wangym.lombok;

import com.wangym.lombok.job.impl.migration.OpenFeignMigrationJob;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

/**
 * @Author: wangym
 * @Date: 2022/7/15 19:47
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class OpenFeignMigrationJobTest {
    @Autowired
    private OpenFeignMigrationJob job;

    @Test
    public void handle() throws IOException {
        job.handle(new ClassPathResource("AppFeignClient.java").getFile());
    }
}
