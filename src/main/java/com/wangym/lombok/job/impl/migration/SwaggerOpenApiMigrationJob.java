package com.wangym.lombok.job.impl.migration;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.wangym.lombok.job.AbstractJavaJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Author: wangym
 * @Date: 2022/8/26 17:34
 */
@ConditionalOnProperty(value = "springcloud2.migration.enable", havingValue = "true")
@Component
@Slf4j
public class SwaggerOpenApiMigrationJob extends AbstractJavaJob {
    @Override
    public void process(CompilationUnit compilationUnit) {
        //https://springdoc.org/#migrating-from-springfox
        compilationUnit.accept(new ApiVisitor(), null);
        compilationUnit.accept(new ApiOperationVisitor(), null);
        compilationUnit.accept(new ApiModelPropertyVisitor(), null);
        compilationUnit.accept(new ApiParamVisitor(), null);
    }

    private Map<String, MemberValuePair> pairsToMap(NodeList<MemberValuePair> pairs) {
        //support null value
        return Optional.ofNullable(pairs)
                .orElse(new NodeList<>())
                .stream()
                //toMap
                .collect(Collectors.toMap(it -> it.getName().asString(), (x) -> x));
    }

    class ApiVisitor extends ModifierVisitor<Void> {
        private AnnotationMetaModel model = new AnnotationMetaModel("Api", "Tag", "io.swagger.annotations.Api", "io.swagger.v3.oas.annotations.tags.Tag");

        @Override
        public Visitable visit(NormalAnnotationExpr n, Void arg) {
            if (n.getName().equals(model.getAnnName())) {
                //修改导入的包
                replaceImportsIfExist(n.findCompilationUnit().get(), model.getImportPackage(), model.getNewImportPackage());

                Map<String, MemberValuePair> map = pairsToMap(n.getPairs());
                NodeList<MemberValuePair> pairs = new NodeList<>();
                //字段名叫name比叫value好，简明扼要，表达清晰
                MemberValuePair name = map.get("name");
                MemberValuePair description = map.get("description");
                if (description != null) {
                    pairs.add(new MemberValuePair("description", description.getValue()));
                }
                if (name != null) {
                    pairs.add(new MemberValuePair("name", name.getValue()));
                } else {
                    ClassOrInterfaceDeclaration parent = n.findAncestor(ClassOrInterfaceDeclaration.class).get();
                    pairs.add(new MemberValuePair("name", new StringLiteralExpr(parent.getName().asString())));
                }
                return new NormalAnnotationExpr(model.getNewAnnNameClone(), pairs);
            }
            return super.visit(n, arg);
        }
    }

    class ApiOperationVisitor extends ModifierVisitor<Void> {
        private AnnotationMetaModel model = new AnnotationMetaModel("ApiOperation", "Operation", "io.swagger.annotations.ApiOperation", "io.swagger.v3.oas.annotations.Operation");

        @Override
        public Visitable visit(NormalAnnotationExpr n, Void arg) {
            if (n.getName().equals(model.getAnnName())) {
                //修改导入的包
                replaceImportsIfExist(n.findCompilationUnit().get(), model.getImportPackage(), model.getNewImportPackage());

                Map<String, MemberValuePair> map = pairsToMap(n.getPairs());
                NodeList<MemberValuePair> pairs = new NodeList<>();
                //字段名叫name比叫value好，简明扼要，表达清晰
                MemberValuePair value = map.get("value");
                MemberValuePair notes = map.get("notes");
                return getNormalAnnotationExpr(n, pairs, value, notes);
            }
            return super.visit(n, arg);
        }

        private NormalAnnotationExpr getNormalAnnotationExpr(AnnotationExpr n, NodeList<MemberValuePair> pairs, MemberValuePair value, MemberValuePair notes) {
            if (notes != null) {
                pairs.add(new MemberValuePair("description", notes.getValue()));
            }
            if (value != null) {
                pairs.add(new MemberValuePair("summary", value.getValue()));
            } else {
                ClassOrInterfaceDeclaration parent = n.findAncestor(ClassOrInterfaceDeclaration.class).get();
                pairs.add(new MemberValuePair("summary", new StringLiteralExpr(parent.getName().asString())));
            }
            return new NormalAnnotationExpr(model.getNewAnnNameClone(), pairs);
        }

        @Override
        public Visitable visit(SingleMemberAnnotationExpr n, Void arg) {
            if (n.getName().equals(model.getAnnName())) {
                //修改导入的包
                replaceImportsIfExist(n.findCompilationUnit().get(), model.getImportPackage(), model.getNewImportPackage());

                NodeList<MemberValuePair> pairs = new NodeList<>();
                pairs.add(new MemberValuePair("summary", n.getMemberValue()));
                return new NormalAnnotationExpr(model.getNewAnnNameClone(), pairs);
            }
            return super.visit(n, arg);
        }
    }

    class ApiModelPropertyVisitor extends ModifierVisitor<Void> {
        private AnnotationMetaModel model = new AnnotationMetaModel("ApiModelProperty", "Schema", "io.swagger.annotations.ApiModelProperty", "io.swagger.v3.oas.annotations.media.Schema");

        @Override
        public Visitable visit(SingleMemberAnnotationExpr n, Void arg) {
            if (n.getName().equals(model.getAnnName())) {
                //修改导入的包
                replaceImportsIfExist(n.findCompilationUnit().get(), model.getImportPackage(), model.getNewImportPackage());

                NodeList<MemberValuePair> pairs = new NodeList<>();
                pairs.add(new MemberValuePair("name", n.getMemberValue()));
                return new NormalAnnotationExpr(model.getNewAnnNameClone(), pairs);
            }
            return super.visit(n, arg);
        }
    }

    class ApiParamVisitor extends ModifierVisitor<Void> {
        private AnnotationMetaModel model = new AnnotationMetaModel("ApiParam", "Parameter", "io.swagger.annotations.ApiParam", "io.swagger.v3.oas.annotations.Parameter");

        @Override
        public Visitable visit(SingleMemberAnnotationExpr n, Void arg) {
            if (n.getName().equals(model.getAnnName())) {
                //修改导入的包
                replaceImportsIfExist(n.findCompilationUnit().get(), model.getImportPackage(), model.getNewImportPackage());

                NodeList<MemberValuePair> pairs = new NodeList<>();
                pairs.add(new MemberValuePair("name", n.getMemberValue()));
                return new NormalAnnotationExpr(model.getNewAnnNameClone(), pairs);
            }
            return super.visit(n, arg);
        }
    }
}
