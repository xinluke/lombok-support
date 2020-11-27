package com.wangym.lombok;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.wangym.lombok.job.impl.JunitJob;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

/**
 * @author wangym
 * @version 创建时间：2018年12月27日 上午11:03:54
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class JunitJobTest {
    @Autowired
    private JunitJob job;

    @Test
    public void handle() throws IOException {
        job.handle(new ClassPathResource("DateUtil.java").getFile());
    }

    @Test
    public void handle1() throws IOException {
        CompilationUnit compilationUnit = StaticJavaParser
                .parse(new ClassPathResource("SimpleDateUtil.java").getInputStream());
        LexicalPreservingPrinter.setup(compilationUnit);
        compilationUnit.accept(new TestVisitor(), null);
        // 使用词法打印机设置对象，以便于保存原本的语法格式
        String newBody = LexicalPreservingPrinter.print(compilationUnit);
        // System.out.println(compilationUnit);
        System.out.println(newBody);
    }
    
    @Test
    public void handle2() throws IOException {
    	job.handle(new ClassPathResource("Application.java").getFile());
    }

    class TestVisitor extends ModifierVisitor<Void> {

        @Override
        public Visitable visit(MethodDeclaration n, Void arg) {
            NodeList<Modifier> set = n.getModifiers();
            set.remove(Modifier.staticModifier());
            n.getAnnotations().add(new MarkerAnnotationExpr("Test"));
            n.getParameters().clear();
            return super.visit(n, arg);
        }
    }

}
