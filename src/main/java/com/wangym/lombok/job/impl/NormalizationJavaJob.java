package com.wangym.lombok.job.impl;

import com.wangym.lombok.job.JavaJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;

@Component
@Slf4j
public class NormalizationJavaJob extends JavaJob {
    @Override
    public void handle(File file) throws IOException {
        byte[] bytes = FileCopyUtils.copyToByteArray(file);
        String code = new String(bytes, "utf-8");
        //rule:(Java) Modifiers should be declared in the correct order
        //Reorder the modifiers to comply with the Java Language Specification.
        String newBody = code.replaceAll(" final static ", " static final ");
        // 以utf-8编码的方式写入文件中
        if (!newBody.equals(code)) {
            FileCopyUtils.copy(newBody.toString().getBytes("utf-8"), file);
        }
    }
}
