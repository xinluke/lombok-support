package com.wangym.lombok.job.impl.migration;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.wangym.lombok.job.AbstractJavaJob;
import com.wangym.lombok.job.Metadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * @Author: wangym
 * @Date: 2022/7/14 18:57
 */
@ConditionalOnProperty(value = "springcloud2.migration.enable", havingValue = "true")
@Component
@Slf4j
public class OpenFeignMigrationJob extends AbstractJavaJob {


    public static final String CONTEXT_ID = "contextId";

    @Override
    public void process(CompilationUnit compilationUnit) {
        compilationUnit.accept(new FeignClientVisitor(), null);
        compilationUnit.accept(new EnableFeignClientsVisitor(), null);
        compilationUnit.accept(new Junit5Visitor(), null);
    }

    class FeignClientVisitor extends ModifierVisitor<Void> {
        private Name targetExpr = new Name("FeignClient");
        private Metadata deleteMeta = new Metadata("FeignClient", "org.springframework.cloud.netflix.feign.FeignClient");
        private Metadata addMeta = new Metadata("FeignClient", "org.springframework.cloud.openfeign.FeignClient");

        @Override
        public Visitable visit(NormalAnnotationExpr n, Void arg) {
            if (n.getName().equals(targetExpr)) {
                NodeList<MemberValuePair> pairs = n.getPairs();
                boolean exist = pairs.stream()
                        .filter(it -> it.getName().equals(new SimpleName(CONTEXT_ID)))
                        .count() > 0;
                if (!exist) {
                    //补充contextId字段值，设置为类名
                    pairs.add(new MemberValuePair(CONTEXT_ID, new StringLiteralExpr(n.getNameAsString())));
                }
                //修改导入的包
                replaceImportsIfExist(n.findCompilationUnit().get(), deleteMeta, addMeta);
            }
            return super.visit(n, arg);
        }

    }

    class EnableFeignClientsVisitor extends ModifierVisitor<Void> {
        private Name targetExpr = new Name("EnableFeignClients");
        private Metadata deleteMeta = new Metadata("Slf4j", "org.springframework.cloud.netflix.feign.EnableFeignClients");
        private Metadata addMeta = new Metadata("Slf4j", "org.springframework.cloud.openfeign.EnableFeignClients");

        @Override
        public Visitable visit(NormalAnnotationExpr n, Void arg) {
            if (n.getName().equals(targetExpr)) {
                //修改导入的包
                replaceImportsIfExist(n.findCompilationUnit().get(), deleteMeta, addMeta);
            }
            return super.visit(n, arg);
        }

        @Override
        public Visitable visit(MarkerAnnotationExpr n, Void arg) {
            // 找出@FeignClient
            if (n.getName().equals(targetExpr)) {
                //修改导入的包
                replaceImportsIfExist(n.findCompilationUnit().get(), deleteMeta, addMeta);
            }
            return super.visit(n, arg);
        }

        private void process(CompilationUnit compilationUnit) {
            replaceImportsIfExist(compilationUnit, deleteMeta, addMeta);
        }

    }

    class Junit5Visitor extends ModifierVisitor<Void> {
        private Name targetExpr = new Name("Test");
        private Name targetExpr2 = new Name("RunWith");
        private Metadata deleteMeta2 = new Metadata("RunWith", "org.junit.runner.RunWith");
        private Metadata deleteMeta = new Metadata("Test", "org.junit.Test");
        private Metadata addMeta = new Metadata("Test", "org.junit.jupiter.api.Test");

        @Override
        public Visitable visit(NormalAnnotationExpr n, Void arg) {
            if (n.getName().equals(targetExpr2)) {
                deleteImports(n.findCompilationUnit().get(), deleteMeta2);
                //在junit5中没有此注解，已不再需要
                return null;
            }
            return super.visit(n, arg);
        }

        @Override
        public Visitable visit(MarkerAnnotationExpr n, Void arg) {
            // 找出@FeignClient
            if (n.getName().equals(targetExpr)) {
                //修改导入的包
                replaceImportsIfExist(n.findCompilationUnit().get(), deleteMeta, addMeta);
            }
            return super.visit(n, arg);
        }
    }
}
