package com.wangym.lombok;

import com.wangym.lombok.job.impl.migration.OpenFeignMigrationJob;
import com.wangym.lombok.job.impl.migration.SwaggerOpenApiMigrationJob;
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
public class SwaggerOpenApiMigrationJobTest {
    @Autowired
    private SwaggerOpenApiMigrationJob job;

    @Test
    public void handle() throws IOException {
        job.handle(new ClassPathResource("ArticleController.java").getFile());
    }
    @Test
    public void handle1() throws IOException {
        job.handle(new ClassPathResource("Application.java").getFile());
    }
    @Test
    public void handle2() throws IOException {
        job.handle(new ClassPathResource("PluginUpdateForm.java").getFile());
    }

    @Test
    public void handle3() throws IOException {
        job.handle(new ClassPathResource("UserServiceTest.java").getFile());
    }
}
