package com.wangym.lombok.job.impl.migration;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.wangym.lombok.job.AbstractJavaJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

import static com.wangym.lombok.job.impl.SystemOutPrintJob.FEIGN_CLIENT;

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
    }

    private Map<String, MemberValuePair> pairsToMap(NodeList<MemberValuePair> pairs) {
        return pairs.stream()
                //toMap
                .collect(Collectors.toMap(it -> it.getName().asString(), (x) -> x));
    }

    class ApiVisitor extends ModifierVisitor<Void> {
        private Name tagetName = new Name("Api");

        @Override
        public Visitable visit(NormalAnnotationExpr n, Void arg) {
            if (n.getName().equals(tagetName)) {
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
                    pairs.add(new MemberValuePair("name", new StringLiteralExpr()));
                }
                return new NormalAnnotationExpr(new Name("Tag"), pairs);
            }
            return super.visit(n, arg);
        }
    }
    class ApiOperationVisitor extends ModifierVisitor<Void> {
        private Name tagetName = new Name("ApiOperation");

        @Override
        public Visitable visit(NormalAnnotationExpr n, Void arg) {
            if (n.getName().equals(tagetName)) {
                Map<String, MemberValuePair> map = pairsToMap(n.getPairs());
                NodeList<MemberValuePair> pairs = new NodeList<>();
                //字段名叫name比叫value好，简明扼要，表达清晰
                MemberValuePair value = map.get("value");
                MemberValuePair notes = map.get("notes");
                if (notes != null) {
                    pairs.add(new MemberValuePair("description", notes.getValue()));
                }
                if (value != null) {
                    pairs.add(new MemberValuePair("summary", value.getValue()));
                } else {
                    pairs.add(new MemberValuePair("summary", new StringLiteralExpr()));
                }
                return new NormalAnnotationExpr(new Name("Operation"), pairs);
            }
            return super.visit(n, arg);
        }
    }
    class ApiModelPropertyVisitor extends ModifierVisitor<Void> {
        private Name tagetName = new Name("ApiModelProperty");

        @Override
        public Visitable visit(MarkerAnnotationExpr n, Void arg) {
            if (n.getName().equals(tagetName)) {
                return new MarkerAnnotationExpr("Schema");
            }
            return super.visit(n, arg);
        }
    }
}
