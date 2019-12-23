package com.wangym.lombok.job.impl;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.wangym.lombok.job.JavaJob;
import com.wangym.lombok.job.WordDict;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import javax.annotation.PreDestroy;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * @author wangym
 * @version 创建时间：2018年11月6日 下午2:27:46
 */
@Component
@Slf4j
public class StatJob extends JavaJob {
    // 单词和数量映射表
    HashMap<String, Integer> hashMap = new HashMap<String, Integer>();
    private WordDict classDict = new WordDict();
    private WordDict methodDict = new WordDict();

    @Override
    public void handle(File file) throws IOException {
        byte[] bytes = FileCopyUtils.copyToByteArray(file);
        CompilationUnit compilationUnit = JavaParser.parse(new String(bytes, "utf-8"));
        StatVisitor visitor = new StatVisitor();
        compilationUnit.clone().accept(visitor, null);
    }

    @PreDestroy
    public void print() throws UnsupportedEncodingException, IOException {
        log.info("print stat info");
        FileCopyUtils.copy(classDict.print().getBytes("utf-8"), new File("classDict.log"));
        FileCopyUtils.copy(methodDict.print().getBytes("utf-8"), new File("methodDict.log"));
    }

    @Getter
    class StatVisitor extends ModifierVisitor<Void> {

        @Override
        public Visitable visit(ClassOrInterfaceDeclaration n, Void arg) {
            // 类名或者接口名
            String nameAsString = n.getNameAsString();
            classDict.add(nameAsString);
            return super.visit(n, arg);
        }

//        @Override
//        public Visitable visit(VariableDeclarator n, Void arg) {
//            System.out.println(n.getNameAsString());
//            return super.visit(n, arg);
//        }
        @Override
        public Visitable visit(MethodDeclaration n, Void arg) {
            String nameAsString = n.getNameAsString();
            methodDict.add(nameAsString);
            return super.visit(n, arg);
        }
    }

}
