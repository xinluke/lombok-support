package com.wangym.lombok;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.wangym.lombok.job.impl.ReplaceLoggerJob;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {

    @Autowired
    private ReplaceLoggerJob job;

    @Test
    public void handle() throws IOException {
        job.handle(new File("c:/CrashLogESHelper.java"));
    }

    @Test
    public void contextLoads() throws IOException {
        File file = new File("c:/AppGroupController.java");
        byte[] bytes = FileCopyUtils.copyToByteArray(file);
        CompilationUnit compilationUnit = JavaParser.parse(new String(bytes, "utf-8"));
        LexicalPreservingPrinter.setup(compilationUnit);
        String newBody = LexicalPreservingPrinter.print(compilationUnit);
        // 以utf-8编码的方式写入文件中
        FileCopyUtils.copy(newBody.getBytes("utf-8"), new File("c:/AppGroupController1.java"));
    }

    @Test
    public void print() throws IOException {
        CompilationUnit cu = JavaParser.parse("class A { }");
        LexicalPreservingPrinter.setup(cu);
        cu.findAll(ClassOrInterfaceDeclaration.class).stream()
                .forEach(c -> {
                    String oldName = c.getNameAsString();
                    String newName = "Abstract" + oldName;
                    System.out.println("Renaming class " + oldName + " into " + newName);
                    c.setName(newName);
                });
        String newBody = LexicalPreservingPrinter.print(cu);
        System.out.println(newBody);
    }

}
