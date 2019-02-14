package com.wangym.lombok;

import com.wangym.lombok.job.MavenDependencyVersionReplaceJob;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MavenDependencyVersionTests {

    @Autowired
    private MavenDependencyVersionReplaceJob job;

    @Test
    public void handle() throws IOException {
        job.exec(new File("c:/pom.xml"));
    }

}
