package com.wangym.lombok;

import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.wangym.lombok.job.impl.ReplaceGeneralCodeJob;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author wangym
 * @version 创建时间：2018年6月7日 上午9:56:09
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ReplaceGeneralCodeJobTest {

    @Autowired
    private ReplaceGeneralCodeJob job;

    @Test
    public void handle() throws IOException {
        job.handle(new ClassPathResource("GeneralCodeExample.java").getFile());
    }

    @Test
    public void print() throws IOException {
        System.setProperty("line.separator", "\n");
        CompilationUnit compilationUnit = StaticJavaParser.parse("package com.wangym.test;\nclass A{ }");
        LexicalPreservingPrinter.setup(compilationUnit);
        NodeList<ImportDeclaration> imports = compilationUnit.getImports();
        String str = "lombok.Data";
        imports.add(new ImportDeclaration(str, false, false));
        String newBody = LexicalPreservingPrinter.print(compilationUnit);
        FileCopyUtils.copy(newBody.getBytes("utf-8"), new File("c:/A.java"));
    }
}
