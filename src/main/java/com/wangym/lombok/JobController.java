package com.wangym.lombok;

import com.wangym.lombok.job.Job;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * @author wangym
 * @version 创建时间：2018年6月28日 下午1:32:48
 */
@Service
@Slf4j
public class JobController {

    @Autowired
    private List<Job> jobList;

    public void start() {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        // 以当前jar为基点，搜索下面全部的文件
        try {
            Resource[] resources = resolver.getResources("file:./**");
            for (Resource resource : resources) {
                diapatch(resource);
            }
            log.info("执行完成");
        } catch (IOException e) {
            log.info("未执行完成", e);
        }
    }

    private void diapatch(Resource resource) {
        for (Job job : jobList) {
            if (job.canRead(resource.getFilename())) {
                try {
                    job.handle(resource.getFile());
                } catch (Exception e) {
                    try {
                        String path = resource.getFile().getPath();
                        log.info("处理文件失败：{}", path, e);
                    } catch (IOException ex) {
                        // ignore
                    }
                }
            }
        }
    }
}
