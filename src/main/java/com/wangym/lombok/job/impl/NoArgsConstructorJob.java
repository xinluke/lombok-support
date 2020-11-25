package com.wangym.lombok.job.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.wangym.lombok.job.AbstractJavaJob;
import com.wangym.lombok.job.Metadata;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author wangym
 * @version 创建时间：2018年12月27日 上午10:48:35
 */
@Component
@Slf4j
public class NoArgsConstructorJob extends AbstractJavaJob {

    private Metadata meta = new Metadata("NoArgsConstructor", "lombok.NoArgsConstructor");

    @Override
    public void process(CompilationUnit compilationUnit) {
        int before = compilationUnit.hashCode();
        NoArgsConstructorVisitor visitor = new NoArgsConstructorVisitor();
        compilationUnit.accept(visitor, null);
        // 如果存在变更，则操作
        if (before != compilationUnit.hashCode()) {
            addImports(compilationUnit, meta);
        }
    }

    @Getter
    class NoArgsConstructorVisitor extends ModifierVisitor<Void> {

        @Override
        public Visitable visit(ConstructorDeclaration n, Void arg) {
            if (n.getModifiers().contains(Modifier.publicModifier())) {
                String nameAsString = n.getNameAsString();
                log.debug("读取到无参构造方法：{}", nameAsString);
                String body = n.getBody().toString().trim();
                // 去除语法块中的'{'和'}'字符
                body = body.substring(1, body.length() - 1);
                // 去除不可见的字符
                body = StringUtils.strip(body);
                // 如果是没有内容的话，是可以替换成@NoArgsConstructor注解的形式的
                if (StringUtils.isBlank(body)) {
                    log.info("替换无参构造方法：{}", nameAsString);
                    ClassOrInterfaceDeclaration parent = n.findAncestor(ClassOrInterfaceDeclaration.class).get();
                    addAnnotation(parent, meta);
                    return null;
                }
            }
            return super.visit(n, arg);
        }

    }

}
