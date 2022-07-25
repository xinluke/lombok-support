package com.wangym.lombok;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.emitter.Emitter;
import org.yaml.snakeyaml.events.Event;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: wangym
 * @Date: 2022/7/22 22:01
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class YamlJobTest {
    @Test
    public void handle() throws IOException {
        LoaderOptions lopts = new LoaderOptions();
        lopts.setProcessComments(true);
        Yaml yaml = new Yaml(lopts);
        Resource resource = new ClassPathResource("test1/application.yml");
        Iterable<Event> res = yaml.parse(new InputStreamReader(resource.getInputStream()));
        List<Event> data = new ArrayList<>();
        for (Event event : res) {
            System.out.println(event);
            data.add(event);
        }
        System.out.println("YamlJobTest.handle");
        //使用底层api来处理event
        DumperOptions dopts = new DumperOptions();
        dopts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dopts.setProcessComments(true);
        dopts.setIndent(2);
        StringWriter output = new StringWriter();
        Emitter emitter = new Emitter(output, dopts);
        for (Event event : data) {
            emitter.emit(event);
        }
        String text = output.toString();

        System.out.println("text = " + text);

    }
}
