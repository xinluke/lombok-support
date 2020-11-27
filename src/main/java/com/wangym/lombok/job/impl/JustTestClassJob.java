package com.wangym.lombok.job.impl;

import com.wangym.lombok.job.AbstractJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
@ConditionalOnProperty(value = "move.resolver.enable", havingValue = "true")
@Component
@Slf4j
public class JustTestClassJob extends AbstractJob {

    public JustTestClassJob() {
        super(".java");
    }

    @Override
    public void exec(File file) throws IOException {
        // 如果在src\main\java下面的Test类，则移动到src\test\java
        String path = file.getPath();
        if (file.getName().endsWith("Test.java") && path.contains("src\\main\\java")) {
            String newPath = path.replace("src\\main\\java", "src\\test\\java");
            log.info("file {} remove to {}",path,newPath);
            file.renameTo(new File(newPath));
        }
    }

}
