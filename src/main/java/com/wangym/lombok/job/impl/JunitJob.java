package com.wangym.lombok.job.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.wangym.lombok.job.AbstractJavaJob;
import com.wangym.lombok.job.Metadata;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * @author wangym
 * @version 创建时间：2019年1月4日 下午6:10:32
 */
@ConditionalOnProperty(value = "junit.resolver.enable", havingValue = "true")
@Component
@Slf4j
public class JunitJob extends AbstractJavaJob {
    private Metadata meta = new Metadata("Test", "org.junit.Test");

    @Override
    public void process(CompilationUnit compilationUnit) {
        MainMehtodVisitor visitor = new MainMehtodVisitor();
        compilationUnit.accept(visitor, null);
    }

    @Override
    public String afterProcess(CompilationUnit compilationUnit) {
        // 此处需要
        JunitMehtodVisitor visitor = new JunitMehtodVisitor();
        compilationUnit.accept(visitor, null);
        return visitor.getName() + ".java";
    }

    @Getter
    class JunitMehtodVisitor extends ModifierVisitor<Void> {
        private String name;

        @Override
        public Visitable visit(ClassOrInterfaceDeclaration n, Void arg) {
            // 按junit的格式重命名类名
            String name = n.getNameAsString();
            if(!name.endsWith("Test")) {
                name = name + "Test";
            }
            n.setName(name);
            return super.visit(n, arg);
        }

        @Override
        public Visitable visit(MethodDeclaration n, Void arg) {
            NodeList<Modifier> set = n.getModifiers();
            String methodName = n.getNameAsString();
            boolean flag = set.contains(Modifier.staticModifier()) && set.contains(Modifier.publicModifier());
            if (flag && "main".equals(methodName)) {
                ClassOrInterfaceDeclaration parent = n.findAncestor(ClassOrInterfaceDeclaration.class).get();
                if (isNotSpringBootMain(parent)) {
                    log.info("找到对应的方法:{}");
                    // 去掉static方法标识符
                    // 加@Test注解
                    // 去掉无用的方法参数
                    set.remove(Modifier.staticModifier());
                    n.getAnnotations().add(new MarkerAnnotationExpr("Test"));
                    n.getParameters().clear();
                    addImports(n.findCompilationUnit().get(), meta);
                }

            } else {
                // 只保留静态方法
                if (!set.contains(Modifier.staticModifier())) {
                    return null;
                }
            }
            return super.visit(n, arg);
        }
    }

    class MainMehtodVisitor extends ModifierVisitor<Void> {
        @Override
        public Visitable visit(MethodDeclaration n, Void arg) {
            NodeList<Modifier> set = n.getModifiers();
            String methodName = n.getNameAsString();
            boolean flag = set.contains(Modifier.staticModifier()) && set.contains(Modifier.publicModifier());
            if (flag && "main".equals(methodName)) {
                ClassOrInterfaceDeclaration parent = n.findAncestor(ClassOrInterfaceDeclaration.class).get();
                if (isNotSpringBootMain(parent)) {
                    // 删除掉main入口方法
                	return null;
                }
            }
            return super.visit(n, arg);
        }
    }

    private boolean isNotSpringBootMain(ClassOrInterfaceDeclaration parent) {
        NodeList<AnnotationExpr> anns = parent.getAnnotations();
        boolean notExist = anns.stream()
                .filter(it -> "SpringBootApplication".equals(it.getNameAsString()))
                .count() == 0;
        return notExist;
    }

}
