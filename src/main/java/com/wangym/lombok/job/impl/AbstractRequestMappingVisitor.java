package com.wangym.lombok.job.impl;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.visitor.ModifierVisitor;

import java.util.List;

public class AbstractRequestMappingVisitor extends ModifierVisitor<Void> {

    protected NormalAnnotationExpr getTargetAnn(MethodDeclaration it, List<String> annNames) {
        NodeList<AnnotationExpr> anns = it.getAnnotations();
        for (AnnotationExpr item : anns) {
            if (annNames.contains(item.getNameAsString())) {
                if (item instanceof NormalAnnotationExpr) {
                    return (NormalAnnotationExpr) item;
                }
            }
        }
        return null;
    }
    protected SingleMemberAnnotationExpr getSingleTargetAnn(MethodDeclaration it, List<String> annNames) {
        NodeList<AnnotationExpr> anns = it.getAnnotations();
        for (AnnotationExpr item : anns) {
            if (annNames.contains(item.getNameAsString())) {
                if (item instanceof SingleMemberAnnotationExpr) {
                    return (SingleMemberAnnotationExpr) item;
                }
            }
        }
        return null;
    }

    protected NormalAnnotationExpr getTargetAnn(ClassOrInterfaceDeclaration it, List<String> annNames) {
        NodeList<AnnotationExpr> anns = it.getAnnotations();
        for (AnnotationExpr item : anns) {
            if (annNames.contains(item.getNameAsString())) {
                if (item instanceof NormalAnnotationExpr) {
                    return (NormalAnnotationExpr) item;
                }
            }
        }
        return null;
    }
    protected SingleMemberAnnotationExpr getSingleTargetAnn(ClassOrInterfaceDeclaration it, List<String> annNames) {
        NodeList<AnnotationExpr> anns = it.getAnnotations();
        for (AnnotationExpr item : anns) {
            if (annNames.contains(item.getNameAsString())) {
                if (item instanceof SingleMemberAnnotationExpr) {
                    return (SingleMemberAnnotationExpr) item;
                }
            }
        }
        return null;
    }
}
