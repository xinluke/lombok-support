package com.wangym.lombok.job.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.wangym.lombok.job.AbstractJavaJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @author wangym
 * @version 创建时间：2018年8月2日 下午8:31:53
 */
@Component
@Slf4j
public class RestControllerJob extends AbstractJavaJob {

    @Override
    public void process(CompilationUnit compilationUnit) {
        ResponseBodyConstructorVisitor visitor = new ResponseBodyConstructorVisitor();
        compilationUnit.clone().accept(visitor, null);
    }

    class ResponseBodyConstructorVisitor extends ModifierVisitor<Void> {

        @Override
        public Visitable visit(ClassOrInterfaceDeclaration n, Void arg) {
            if (isTarget(n)) {
                for (MethodDeclaration method : n.getMethods()) {
                    NodeList<AnnotationExpr> anns = method.getAnnotations();
                    test(anns);
                }
                return n;
            }
            return super.visit(n, arg);
        }

        private boolean isTarget(ClassOrInterfaceDeclaration it) {
            List<String> annNames = Arrays.asList("RestController");
            NodeList<AnnotationExpr> anns = it.getAnnotations();
            for (AnnotationExpr item : anns) {
                if (annNames.contains(item.getNameAsString())) {
                    return true;
                }
            }
            return false;
        }

        private void test(NodeList<AnnotationExpr> list) {
            List<String> mappingMethod = Arrays.asList("RequestMapping", "GetMapping", "PostMapping", "PutMapping",
                    "DeleteMapping", "PatchMapping");
            boolean isMappingMethod = list.stream()
                    .filter(it -> mappingMethod.contains(it.getNameAsString()))
                    .count() == 1;
            if (isMappingMethod) {
                AnnotationExpr expr = getUnusedAnnotationExpr(list);
                if (expr != null) {
                    expr.remove();
                }
            }
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

}
