package com.wangym.lombok.job;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author wangym
 * @version 创建时间：2018年11月6日 下午2:27:46
 */
@Component
@Slf4j
public class SystemOutPrintJob extends AbstractJob {

    public SystemOutPrintJob() {
        super(".java");
    }

    @Override
    public void handle(File file) throws IOException {
        byte[] bytes = FileCopyUtils.copyToByteArray(file);
        CompilationUnit compilationUnit = JavaParser.parse(new String(bytes, "utf-8"));
        List<ClassMehodCallMapping> methodCallList = extractAllMethodCallExpr(compilationUnit);
        if (methodCallList.isEmpty()) {
            return;
        }
        log.info("存在[System.out.println()]代码块，将进行替换");
        LexicalPreservingPrinter.setup(compilationUnit);
        methodCallList.forEach(it -> process(it.getExprs()));
        methodCallList.forEach(it -> addAnnotation(it.getC()));
        deleteImports(compilationUnit);
        String newBody = LexicalPreservingPrinter.print(compilationUnit);
        // 以utf-8编码的方式写入文件中
        FileCopyUtils.copy(newBody.toString().getBytes("utf-8"), file);
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
                .filter(it -> str.equals(it.getName().asString()))
                .count() == 0;
        if (notExist) {
            imports.add(new ImportDeclaration(str, false, false));
        }
    }

    private List<ClassMehodCallMapping> extractAllMethodCallExpr(CompilationUnit compilationUnit) {
        List<ClassOrInterfaceDeclaration> classList = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
        List<ClassMehodCallMapping> result = new ArrayList<>();
        for (ClassOrInterfaceDeclaration c : classList) {
            // 不是顶级类就跳过，因为会递归查出这个类下面关联的全部方法调用代码块
            if (!c.isTopLevelType()) {
                continue;
            }
            result.add(new ClassMehodCallMapping(c) {

                @Override
                boolean filter(MethodCallExpr it) {
                    Optional<Expression> scope = it.getScope();
                    if (scope.isPresent() && "System.out".equals(scope.get().toString())
                            && "println".equals(it.getNameAsString())) {
                        return true;
                    }
                    return false;
                }

            });
        }
        return result.stream()
                .filter(item -> {
                    return !item.getExprs().isEmpty();
                })
                .collect(Collectors.toList());
    }

    private void process(List<MethodCallExpr> exprs) {
        exprs.forEach(this::process);
    }

    private void process(MethodCallExpr expr) {
        String str = "log";
        expr.setScope(new NameExpr(str));
        expr.setName("info");
    }

}
