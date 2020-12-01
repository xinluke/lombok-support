package com.wangym.lombok;

import com.wangym.lombok.job.impl.ReplaceRequestMappingJob;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

/**
 * @author wangym
 * @version 创建时间：2018年8月1日 上午11:03:32
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ReplaceRequestMappingJobTest {

    @Autowired
    private ReplaceRequestMappingJob job;

    @Test
    public void handle() throws IOException {
        job.setEnableMergeRequestUrl(true);
        job.setShowDetail(true);
        job.handle(new ClassPathResource("SdkBlackListServiceClient.java").getFile());
    }

    @Test
    public void handle1() throws IOException {
        job.setEnableMergeRequestUrl(true);
        job.setShowDetail(true);
        job.handle(new ClassPathResource("SdkBlackListServiceCtl.java").getFile());
    }
}
