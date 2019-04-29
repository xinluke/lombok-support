package com.wangym.lombok.job.impl;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author wangym
 * @version 创建时间：2018年5月14日 上午10:30:27
 */
@Component
@Slf4j
public class ReplaceLoggerJob extends JavaJob {

    private Metadata meta = new Metadata("Slf4j", "lombok.extern.slf4j.Slf4j");

    @Override
    public void handle(File file) throws IOException {
        byte[] bytes = FileCopyUtils.copyToByteArray(file);
        CompilationUnit compilationUnit = JavaParser.parse(new String(bytes, "utf-8"));
        LoggerVisitor visitor = new LoggerVisitor();
        // 预检查
        compilationUnit.clone().findAll(FieldDeclaration.class).forEach(it -> visitor.visit(it, null));
        if (visitor.isModify()) {
            log.info("清除原来的log声明，替换成@slf4j形式");
            LexicalPreservingPrinter.setup(compilationUnit);
            // 替换成@slf4j注解
            compilationUnit.accept(visitor, null);
            // rename变量
            String loggerName = visitor.getLoggerName();
            compilationUnit.accept(new LoggerRenameVisitor(loggerName), null);
            addImports(compilationUnit, meta);
            deleteImports(compilationUnit);
            String newBody = LexicalPreservingPrinter.print(compilationUnit);
            // 以utf-8编码的方式写入文件中
            FileCopyUtils.copy(newBody.toString().getBytes("utf-8"), file);
        }
    }

    @Getter
    class LoggerVisitor extends ModifierVisitor<Void> {
        private boolean modify = false;
        private String loggerName;

        @Override
        public Visitable visit(FieldDeclaration field, Void arg) {
            if (!field.isPrivate()) {
                return super.visit(field, arg);
            }
            List<VariableDeclarator> variableList = field.findAll(VariableDeclarator.class);
            // 假设Logger的声明都是一个变量一个声明，不存在int x=3,y=4;这样的形式
            if (variableList.size() == 1) {
                VariableDeclarator variable = variableList.get(0);
                // 找出关于"Logger"的字段声明
                String typeAsString = variable.getTypeAsString();
                if (Arrays.asList("Logger", "Log").contains(typeAsString)) {
                    Optional<Expression> initializer = variable.getInitializer();
                    Expression exp = initializer.get();
                    if (exp instanceof MethodCallExpr) {
                        String text = ((MethodCallExpr) exp).getArgument(0).toString();
                        ClassOrInterfaceDeclaration parent = field.findParent(ClassOrInterfaceDeclaration.class).get();
                        String className = parent.getNameAsString();
                        List<String> targetList = Arrays.asList(className, "getClass");
                        for (String target : targetList) {
                            boolean check = text.contains(target);
                            if (check) {
                                // 设置标志位
                                modify = true;
                                // 保存loggerName
                                loggerName = variable.getNameAsString();
                                addAnnotation(parent, meta);
                                // 删除掉代码写的Logger声明，换成注解形式
                                return null;
                            }
                        }
                    }
                }
            }
            return super.visit(field, arg);
        }

    }

    @Getter
    class LoggerRenameVisitor extends ModifierVisitor<Void> {
        private String loggerName;

        public LoggerRenameVisitor(String loggerName) {
            super();
            this.loggerName = loggerName;
        }

        @Override
        public Visitable visit(MethodCallExpr n, Void arg) {
            if (!n.getScope().isPresent()) {
                return super.visit(n, arg);
            }
            if (loggerName.equals(n.getScope().get().toString())) {
                renameLogger(n);
                return n;
            }
            return super.visit(n, arg);
        }

        private void renameLogger(MethodCallExpr loggerCall) {
            String str = "log";
            // 对于logger的全部方法调用，替换变量名
            loggerCall.setScope(new NameExpr(str));
        }

    }

    private void deleteImports(CompilationUnit compilationUnit) {
        NodeList<ImportDeclaration> imports = compilationUnit.getImports();
        List<String> deleteImports = Arrays.asList(
                "org.slf4j.Logger",
                "org.slf4j.LoggerFactory",
                "org.apache.log4j.Logger");
        imports.stream()
                .filter(it -> {
                    return deleteImports.contains(it.getName().asString());
                })
                // 不可边循环边删除,所以先filter出一个集合再删除
                .collect(Collectors.toList())
                .forEach(it -> compilationUnit.remove(it));
    }

}
