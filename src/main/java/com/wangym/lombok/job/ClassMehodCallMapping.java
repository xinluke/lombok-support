package com.wangym.lombok.job;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author  wangym
* @version 创建时间：2018年11月6日 下午2:19:47
*/
@Getter
public abstract class ClassMehodCallMapping {
    private ClassOrInterfaceDeclaration c;
    private List<MethodCallExpr> exprs;

    public ClassMehodCallMapping(ClassOrInterfaceDeclaration c) {
        super();
        this.c = c;
        List<MethodCallExpr> result = new ArrayList<>();
        result.addAll(c.findAll(MethodCallExpr.class));
        this.exprs = result.stream()
                .filter(this::filter)
                .collect(Collectors.toList());
    }

    abstract boolean filter(MethodCallExpr it);

}