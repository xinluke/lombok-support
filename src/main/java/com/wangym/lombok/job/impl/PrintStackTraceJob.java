package com.wangym.lombok.job.impl;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.wangym.lombok.job.JavaJob;
import com.wangym.lombok.job.Metadata;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author wangym
 * @version 创建时间：2018年11月6日 上午10:29:24
 */
@Component
@Slf4j
public class PrintStackTraceJob extends JavaJob {

    private Metadata meta = new Metadata("Slf4j", "lombok.extern.slf4j.Slf4j");

    @Override
    public void handle(File file) throws IOException {
        byte[] bytes = FileCopyUtils.copyToByteArray(file);
        String code = new String(bytes, "utf-8");
        CompilationUnit compilationUnit = JavaParser.parse(code);
        int before = compilationUnit.hashCode();
        LexicalPreservingPrinter.setup(compilationUnit);
        PrintStackTraceVisitor visitor = new PrintStackTraceVisitor(compilationUnit);
        compilationUnit.accept(visitor, null);
        // 如果存在变更，则操作
        if (before != compilationUnit.hashCode()) {
            String newBody = LexicalPreservingPrinter.print(compilationUnit);
            // 以utf-8编码的方式写入文件中
            FileCopyUtils.copy(newBody.toString().getBytes("utf-8"), file);
        }
    }

    @Getter
    class PrintStackTraceVisitor extends ModifierVisitor<Void> {
        private CompilationUnit compilationUnit;

        public PrintStackTraceVisitor(CompilationUnit compilationUnit) {
            super();
            this.compilationUnit = compilationUnit;
        }
        
        @Override
        public Visitable visit(MethodCallExpr it, Void arg) {
            if ("printStackTrace".equals(it.getName().toString()) && it.getArguments().isEmpty()) {
                log.info("存在[e.printStackTrace()]的代码块，将进行替换");
                ClassOrInterfaceDeclaration parent = it.findParent(ClassOrInterfaceDeclaration.class).get();
                addAnnotation(parent, meta);
                addImports(compilationUnit, meta);
                return process(it);
            }
            return super.visit(it, arg);
        }

        private Visitable process(MethodCallExpr expr) {
            /*
             * 将e.printStackTrace(); 替换成log.error("unexpected exception,please check",e);
             */
            String var = expr.getScope().get().toString();
            String str = "log";
            expr.setScope(new NameExpr(str));
            expr.setName("error");
            expr.getArguments().add(new StringLiteralExpr("unexpected exception,please check"));
            expr.getArguments().add(new NameExpr(var));
            return expr;
        }

    }

}
