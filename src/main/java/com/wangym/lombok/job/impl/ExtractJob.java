package com.wangym.lombok.job.impl;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.wangym.lombok.job.JavaJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;

@Component
@ConditionalOnProperty(value = "extract.enable", havingValue = "true")
@Slf4j
public class ExtractJob extends JavaJob {

    @Override
    public void handle(File file) throws IOException {
        byte[] bytes = FileCopyUtils.copyToByteArray(file);
        CompilationUnit compilationUnit = StaticJavaParser.parse(new String(bytes, "utf-8"));
        ExtractVisitor visitor = new ExtractVisitor();
        LexicalPreservingPrinter.setup(compilationUnit);
        log.info("class文件抽取成接口,{}", file);
        compilationUnit.accept(visitor, null);
        String newBody = LexicalPreservingPrinter.print(compilationUnit);
        // 以utf-8编码的方式写入文件中
        FileCopyUtils.copy(newBody.toString().getBytes("utf-8"), file);
    }

    class ExtractVisitor extends ModifierVisitor<Void> {

        @Override
        public Visitable visit(BlockStmt n, Void arg) {
            // 不要方法体
            return null;
        }

        @Override
        public Visitable visit(ClassOrInterfaceDeclaration n, Void arg) {
            // 换成接口
            if (!n.isInterface()) {
                n.setInterface(true);
            }
            return super.visit(n, arg);
        }

        @Override
        public Visitable visit(FieldDeclaration n, Void arg) {
            // 不要字段声明
            return null;
        }

        @Override
        public Visitable visit(MethodDeclaration n, Void arg) {
            NodeList<Modifier> set = n.getModifiers();
            // 非公有方法的去除掉
            if (!set.contains(Modifier.publicModifier())) {
                return null;
            }
            // 公有方法的修饰符去除，本身在接口中的方法就是public的
            NodeList<Modifier> newset = NodeList.nodeList(set);
            newset.remove(Modifier.publicModifier());
            n.setModifiers(newset);
            return super.visit(n, arg);
        }

    }
}
