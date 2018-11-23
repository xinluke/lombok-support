package com.wangym.lombok.job;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wangym
 * @version 创建时间：2018年8月2日 下午8:31:53
 */
@Component
public class RestControllerJob extends JavaJob {

    @Override
    public void handle(File file) throws IOException {
        byte[] bytes = FileCopyUtils.copyToByteArray(file);
        CompilationUnit compilationUnit = JavaParser.parse(new String(bytes, "utf-8"));
        List<String> annNames = Arrays.asList("RestController");
        List<ClassOrInterfaceDeclaration> cList = compilationUnit.findAll(ClassOrInterfaceDeclaration.class).stream()
                .filter(it -> {
                    NodeList<AnnotationExpr> anns = it.getAnnotations();
                    for (AnnotationExpr item : anns) {
                        if (annNames.contains(item.getNameAsString())) {
                            return true;
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());
        List<AnnotationExpr> annList = new ArrayList<>();
        for (ClassOrInterfaceDeclaration c : cList) {
            for (MethodDeclaration method : c.getMethods()) {
                NodeList<AnnotationExpr> anns = method.getAnnotations();
                AnnotationExpr result = test(anns);
                if (result != null) {
                    annList.add(result);
                }
            }
        }
        if (annList.isEmpty()) {
            return;
        }
        LexicalPreservingPrinter.setup(compilationUnit);
        annList.forEach(it -> it.remove());
        String newBody = LexicalPreservingPrinter.print(compilationUnit);
        // 以utf-8编码的方式写入文件中
        FileCopyUtils.copy(newBody.toString().getBytes("utf-8"), file);
    }

    private AnnotationExpr test(NodeList<AnnotationExpr> list) {
        List<String> mappingMethod = Arrays.asList("RequestMapping", "GetMapping", "PostMapping", "PutMapping",
                "DeleteMapping", "PatchMapping");
        boolean isMappingMethod = list.stream()
                .filter(it -> mappingMethod.contains(it.getNameAsString()))
                .count() == 1;
        if (isMappingMethod) {
            return getUnusedAnnotationExpr(list);
        }
        return null;
    }

    private AnnotationExpr getUnusedAnnotationExpr(NodeList<AnnotationExpr> list) {
        for (AnnotationExpr it : list) {
            if ("ResponseBody".equals(it.getNameAsString())) {
                return it;
            }
        }
        return null;
    }
}
