package com.wangym.lombok.job.impl.migration;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
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
        //springfox的版本（包括3.0），在spring2.6之后出现问题，没办法兼容，需要切换为springdoc的方式
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
                //重新创建新的实例
                .collect(Collectors.toMap(it -> it.getName().asString(), (x) -> x.clone()));
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
                replacedCheck(n, pairs, map);
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
                MemberValuePair hidden = map.get("hidden");
                if (notes != null) {
                    pairs.add(new MemberValuePair("description", notes.getValue()));
                }
                if (hidden != null) {
                    pairs.add(new MemberValuePair("hidden", hidden.getValue()));
                }
                if (value != null) {
                    pairs.add(new MemberValuePair("summary", value.getValue()));
                } else {
                    ClassOrInterfaceDeclaration parent = n.findAncestor(ClassOrInterfaceDeclaration.class).get();
                    pairs.add(new MemberValuePair("summary", new StringLiteralExpr(parent.getName().asString())));
                }
                replacedCheck(n, pairs, map);
                return new NormalAnnotationExpr(model.getNewAnnNameClone(), pairs);
            }
            return super.visit(n, arg);
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
        public Visitable visit(NormalAnnotationExpr n, Void arg) {
            if (n.getName().equals(model.getAnnName())) {
                //修改导入的包
                replaceImportsIfExist(n.findCompilationUnit().get(), model.getImportPackage(), model.getNewImportPackage());

                NodeList<MemberValuePair> pairs = new NodeList<>();
                Map<String, MemberValuePair> map = pairsToMap(n.getPairs());
                MemberValuePair value = map.get("value");
                MemberValuePair name = map.get("name");
                MemberValuePair example = map.get("example");
                MemberValuePair notes = map.get("notes");
                MemberValuePair required = map.get("required");
                if (example != null) {
                    pairs.add(new MemberValuePair("example", example.getValue()));
                }
                if (value != null) {
                    //name属性是字段展示名称，默认就是和字段名一致，如果前端展示字段名和后台字段名不一致，才需要定义
                    pairs.add(new MemberValuePair("description", value.getValue()));
                }
                if (notes != null) {
                    pairs.add(new MemberValuePair("description", notes.getValue()));
                }
                if (name != null) {
                    pairs.add(new MemberValuePair("description", name.getValue()));
                }
                if (required != null) {
                    pairs.add(new MemberValuePair("required", required.getValue()));
                }
                replacedCheck(n, pairs, map);
                return new NormalAnnotationExpr(model.getNewAnnNameClone(), pairs);
            }
            return super.visit(n, arg);
        }

        @Override
        public Visitable visit(SingleMemberAnnotationExpr n, Void arg) {
            if (n.getName().equals(model.getAnnName())) {
                //修改导入的包
                replaceImportsIfExist(n.findCompilationUnit().get(), model.getImportPackage(), model.getNewImportPackage());

                NodeList<MemberValuePair> pairs = new NodeList<>();
                //name属性是字段展示名称，默认就是和字段名一致，如果前端展示字段名和后台字段名不一致，才需要定义
                pairs.add(new MemberValuePair("description", n.getMemberValue()));
                return new NormalAnnotationExpr(model.getNewAnnNameClone(), pairs);
            }
            return super.visit(n, arg);
        }
    }

    private void replacedCheck(NormalAnnotationExpr n, NodeList<MemberValuePair> pairs, Map<String, MemberValuePair> map) {
        //有种情况是新的参数列表会进行补充和自定义添加，所以一定不会小于原来的参数列表
        //常规情况是两者相等，我们做一对一的转化
        if (pairs.size() < map.size()) {
            //如果未完全替换，则抛出异常。避免新代码编译过了，但提交到生产出现问题
            throw new IllegalStateException("Not fully replaced, please check" + n);
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
