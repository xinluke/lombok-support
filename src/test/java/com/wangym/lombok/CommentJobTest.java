package com.wangym.lombok;

import com.wangym.lombok.job.impl.CommentJob;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CommentJobTest {

    @Autowired
    private CommentJob job;

    @Test
    public void handle() throws IOException {
        job.exec(new ClassPathResource("LoggerExample.java").getFile());
    }

}
