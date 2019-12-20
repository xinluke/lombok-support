package com.wangym.lombok.job.impl;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.wangym.lombok.job.JavaJob;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import javax.annotation.PreDestroy;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * @author wangym
 * @version 创建时间：2018年11月6日 下午2:27:46
 */
@Component
@Slf4j
public class StatJob extends JavaJob {
    // 单词和数量映射表
    HashMap<String, Integer> hashMap = new HashMap<String, Integer>();

    @Override
    public void handle(File file) throws IOException {
        byte[] bytes = FileCopyUtils.copyToByteArray(file);
        CompilationUnit compilationUnit = JavaParser.parse(new String(bytes, "utf-8"));
        StatVisitor visitor = new StatVisitor();
        compilationUnit.clone().accept(visitor, null);
    }

    @PreDestroy
    public void print() {
        log.info("print stat info");
        Iterator<String> iterator = hashMap.keySet().iterator();
        while (iterator.hasNext()) {
            String word = iterator.next();

            log.info("单词:{} 出现次数:{}", word, hashMap.get(word));
        }
    }

    @Getter
    class StatVisitor extends ModifierVisitor<Void> {

        @Override
        public Visitable visit(ClassOrInterfaceDeclaration n, Void arg) {
            // 类名或者接口名
            String nameAsString = n.getNameAsString();

            process(nameAsString);
            return super.visit(n, arg);
        }

        private void process(String word) {
            Set<String> wordSet = hashMap.keySet();
            // 如果已经有这个单词了，
            if (wordSet.contains(word)) {
                Integer number = hashMap.get(word);
                number++;
                hashMap.put(word, number);
            } else {
                hashMap.put(word, 1);
            }
        }

//        @Override
//        public Visitable visit(VariableDeclarator n, Void arg) {
//            System.out.println(n.getNameAsString());
//            return super.visit(n, arg);
//        }
        @Override
        public Visitable visit(MethodDeclaration n, Void arg) {
            String nameAsString = n.getNameAsString();
            process(nameAsString);
            return super.visit(n, arg);
        }
    }

}
