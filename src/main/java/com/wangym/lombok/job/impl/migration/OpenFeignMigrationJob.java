package com.wangym.lombok.job.impl.migration;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.wangym.lombok.job.AbstractJavaJob;
import com.wangym.lombok.job.Metadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: wangym
 * @Date: 2022/7/14 18:57
 */
@ConditionalOnProperty(value = "springcloud2.migration.enable", havingValue = "true")
@Component
@Slf4j
public class OpenFeignMigrationJob extends AbstractJavaJob {

    public static final NameExpr ASSERT_SCOPE = new NameExpr("Assert");
    private List<AnnotationMetaModel> paramList;
    public static final String CONTEXT_ID = "contextId";

    public OpenFeignMigrationJob() {
        paramList = new ArrayList<>();
        //junit相关
        paramList.add(new AnnotationMetaModel("Test", "org.junit.Test", "org.junit.jupiter.api.Test"));
        paramList.add(new AnnotationMetaModel("RunWith", "org.junit.runner.RunWith", "org.junit.jupiter.api.Test"));
        paramList.add(new AnnotationMetaModel("Before", "BeforeEach", "org.junit.Before", "org.junit.jupiter.api.BeforeEach"));
        paramList.add(new AnnotationMetaModel("After", "AfterEach", "org.junit.After", "org.junit.jupiter.api.AfterEach"));
        //swagger相关
        paramList.add(new AnnotationMetaModel("ApiModel", "Schema", "io.swagger.annotations.ApiModel", "io.swagger.v3.oas.annotations.media.Schema"));
    }

    @Override
    public void process(CompilationUnit compilationUnit) {
        compilationUnit.accept(new FeignClientVisitor(), null);
        compilationUnit.accept(new EnableFeignClientsVisitor(), null);
        compilationUnit.accept(new Junit5Visitor(), null);
        compilationUnit.accept(new ReplaceEasyvisitor(paramList), null);
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
                    ClassOrInterfaceDeclaration parent = n.findAncestor(ClassOrInterfaceDeclaration.class).get();
                    //补充contextId字段值，设置为类名
                    pairs.add(new MemberValuePair(CONTEXT_ID, new StringLiteralExpr(parent.getNameAsString())));
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
    class ReplaceEasyvisitor extends ModifierVisitor<Void>{
        private List<AnnotationMetaModel> paramList;

        public ReplaceEasyvisitor(List<AnnotationMetaModel> paramList) {
            this.paramList = paramList;
        }

        @Override
        public Visitable visit(MarkerAnnotationExpr n, Void arg) {
            for (AnnotationMetaModel model : paramList) {
                // 找出对应的注解
                if (n.getName().equals(model.getAnnName())) {
                    //修改导入的包
                    replaceImportsIfExist(n.findCompilationUnit().get(), model.getImportPackage(), model.getNewImportPackage());
                    if(!model.getAnnName().equals(model.getNewAnnName())) {
                        //需要更新为新的名称，因为全局就一份，需要clone
                        //n.setName(model.getNewAnnName().clone());
                        //需要重新构建此对象
                        return new MarkerAnnotationExpr(model.getNewAnnName().clone());
                    }
                }
            }
            return super.visit(n, arg);
        }

    }

    class Junit5Visitor extends ModifierVisitor<Void> {
        private Name targetExpr2 = new Name("RunWith");
        private Metadata deleteMeta2 = new Metadata("RunWith", "org.junit.runner.RunWith");

        private AnnotationMetaModel assertionsModel = new AnnotationMetaModel("Assert", "Assertions", "org.junit.Assert", "org.junit.jupiter.api.Assertions");
        @Override
        public Visitable visit(SingleMemberAnnotationExpr n, Void arg) {
            if (n.getName().equals(targetExpr2)) {
                deleteImports(n.findCompilationUnit().get(), deleteMeta2);
                //在junit5中没有此注解，已不再需要
                return null;
            }
            return super.visit(n, arg);
        }

        @Override
        public Visitable visit(MethodCallExpr n, Void arg) {
            //Assert 类的调用等价替换为5中Assertions
            n.getScope()
                    .filter(it -> it.equals(ASSERT_SCOPE))
                    .ifPresent(it -> {
                        n.setScope(new NameExpr("Assertions"));
                        replaceImportsIfExist(n.findCompilationUnit().get(), assertionsModel.getImportPackage(), assertionsModel.getNewImportPackage());
                    });
            return super.visit(n, arg);
        }

    }
}
