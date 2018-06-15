package com.wangym.lombok.job;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.wangym.lombok.job.log.LogPackage;
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
public class ReplaceLoggerJob {

    public void handle(File file) throws IOException {
        byte[] bytes = FileCopyUtils.copyToByteArray(file);
        CompilationUnit compilationUnit = JavaParser.parse(new String(bytes,"utf-8"));
        List<ClassOrInterfaceDeclaration> clazzList = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
        if (clazzList.size() != 1) {
            return;
        }
        ClassOrInterfaceDeclaration c = clazzList.get(0);
        String className = c.getNameAsString();
        List<FieldDeclaration> fields = c.getFields();
        LogPackage pkg = null;
        for (FieldDeclaration field : fields) {
            pkg = getLogPackage(field, className);
            if (pkg != null) {
                break;
            }
        }
        if (pkg != null) {
            LexicalPreservingPrinter.setup(compilationUnit);
            log.info("当前文件符合转换,class name:{},logger name:{}", className, pkg.getLoggerName());
            // 清除原来的log声明
            pkg.getField().remove();
            addAnnotation(c);
            deleteImports(compilationUnit);
            String newBody = LexicalPreservingPrinter.print(compilationUnit);
            newBody = newBody.replaceAll(pkg.getLoggerName() + "\\.", "log\\.");
            // 以utf-8编码的方式写入文件中
            FileCopyUtils.copy(newBody.toString().getBytes("utf-8"), file);
        }
    }

    /**
     * 提取目标为 private作用域，Logger类型，并且右表达式包含类名的FieldDeclaration
     * 
     * @param field
     * @param className
     * @return
     */
    private LogPackage getLogPackage(FieldDeclaration field, String className) {
        if (field.isPrivate()) {
            List<VariableDeclarator> variableList = field.findAll(VariableDeclarator.class);
            // 假设一个FieldDeclaration只定义一个VariableDeclarator
            if (variableList.size() == 1) {
                VariableDeclarator variable = variableList.get(0);
                if ("Logger".equals(variable.getTypeAsString())) {
                    Optional<Expression> initializer = variable.getInitializer();
                    if (initializer.isPresent()) {
                        Expression exp = initializer.get();
                        if (exp instanceof MethodCallExpr) {
                            String text = ((MethodCallExpr) exp).getArgument(0).toString();
                            List<String> targetList = Arrays.asList(className, "getClass");
                            for (String target : targetList) {
                                boolean check = text.contains(target);
                                if (check) {
                                    return new LogPackage(field, variable.getNameAsString());
                                }
                            }
                        }
                    }

                }
            }
        }
        return null;
    }

    private void addAnnotation(ClassOrInterfaceDeclaration c) {
        NodeList<AnnotationExpr> anns = c.getAnnotations();
        String name = "Slf4j";
        boolean notExist = anns.stream()
                .filter(it -> name.equals(it.getNameAsString()))
                .count() == 0;
        if (notExist) {
            anns.add(new MarkerAnnotationExpr(name));
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
        String str = "lombok.extern.slf4j.Slf4j";
        boolean notExist = imports.stream()
                .filter(str::equals)
                .count() == 0;
        if (notExist) {
            imports.add(new ImportDeclaration(str, false, false));
        }
    }

}
