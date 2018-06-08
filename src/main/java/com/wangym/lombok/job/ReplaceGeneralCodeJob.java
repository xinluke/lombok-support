package com.wangym.lombok.job;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wangym
 * @version 创建时间：2018年6月7日 上午9:42:59
 */
@Component
@Slf4j
public class ReplaceGeneralCodeJob {

    public void handle(File file) throws IOException {
        byte[] bytes = FileCopyUtils.copyToByteArray(file);
        CompilationUnit compilationUnit = JavaParser.parse(new String(bytes, "utf-8"));
        List<String> classNames = compilationUnit.findAll(ClassOrInterfaceDeclaration.class).stream()
                .filter(this::test)
                .map(ClassOrInterfaceDeclaration::getNameAsString)
                .collect(Collectors.toList());
        if (!classNames.isEmpty()) {
            LexicalPreservingPrinter.setup(compilationUnit);
            for (String string : classNames) {
                ClassOrInterfaceDeclaration c = compilationUnit.getClassByName(string).get();
                c.getMethods().stream()
                        .forEach(it -> c.remove(it));
                addAnnotation(c);
            }
            log.info("当前文件符合转换,class name:{}", file.getName());
            addImports(compilationUnit);
            String newBody = LexicalPreservingPrinter.print(compilationUnit);
            // 暂时使用正则表达式的方式修正格式错误的问题
            newBody = newBody.replaceAll(";import", ";\n\nimport");
            // 以utf-8编码的方式写入文件中
            FileCopyUtils.copy(newBody.toString().getBytes("utf-8"), file);
        }
    }

    private boolean test(ClassOrInterfaceDeclaration clazz) {
        ClassOrInterfaceDeclaration c = clazz.clone();
        List<FieldDeclaration> fields = c.getFields();
        List<MethodDeclaration> methods = c.getMethods();
        if (!fields.isEmpty() && (fields.size() * 2 == methods.size())) {
            List<String> virtualMethods = fields.stream()
                    .map(it -> {
                        return Arrays.asList(it.createGetter().toString(), it.createSetter().toString());
                    })
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
            // 验证全部的方法是否都是getter和setter
            long count = methods.stream()
                    .filter(it -> {
                        return !virtualMethods.contains(it.toString());
                    })
                    .count();
            return count == 0;
        }
        return false;
    }

    private void addAnnotation(ClassOrInterfaceDeclaration c) {
        NodeList<AnnotationExpr> anns = c.getAnnotations();
        String name = "Data";
        boolean notExist = anns.stream()
                .filter(it -> name.equals(it.getNameAsString()))
                .count() == 0;
        if (notExist) {
            anns.add(new MarkerAnnotationExpr(name));
        }
    }

    private void addImports(CompilationUnit compilationUnit) {
        NodeList<ImportDeclaration> imports = compilationUnit.getImports();
        String str = "lombok.Data";
        boolean notExist = imports.stream()
                .filter(str::equals)
                .count() == 0;
        if (notExist) {
            imports.add(new ImportDeclaration(str, false, false));
        }
    }
}
