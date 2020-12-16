package com.wangym.lombok.job.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.wangym.lombok.job.AbstractJavaJob;
import com.wangym.lombok.job.Metadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author wangym
 * @version 创建时间：2018年11月6日 下午2:27:46
 */
@Component
@Slf4j
public class SystemOutPrintJob extends AbstractJavaJob {
    private Metadata meta = new Metadata("Slf4j", "lombok.extern.slf4j.Slf4j");

    @Override
    public void process(CompilationUnit compilationUnit) {
        SystemOutPrintVisitor visitor = new SystemOutPrintVisitor(compilationUnit);
        compilationUnit.accept(new MainMehtodVisitor(), null);
        compilationUnit.accept(visitor, null);
        compilationUnit.accept(new FeignClientVisitor(), null);
    }

    class FeignClientVisitor extends ModifierVisitor<Void> {

        @Override
        public Visitable visit(ClassOrInterfaceDeclaration n, Void arg) {
            // 找出@FeignClient
            boolean flag = false;
            NodeList<AnnotationExpr> anns = n.getAnnotations();
            for (AnnotationExpr annotationExpr : anns) {
                if (annotationExpr.toString().startsWith("@FeignClient")) {
                    if (annotationExpr instanceof NormalAnnotationExpr) {
                        NormalAnnotationExpr a = (NormalAnnotationExpr) annotationExpr;
                        long count = a.getPairs().stream().filter(it -> it.getName().toString().equals("url")).count();
                        if (count == 0) {
                            flag = true;
                            break;
                        }
                    } else {
                        flag = true;
                        break;
                    }
                }
            }
            if (flag && n.isInterface() && n.getExtendedTypes().isEmpty()) {
                String name = getQualifiedName(n);
                log.info("find target FeignClient:{},path:{}", n.getNameAsString(), name);
            }
            return super.visit(n, arg);
        }

        private String getQualifiedName(ClassOrInterfaceDeclaration n) {
            PackageDeclaration pkg = n.findCompilationUnit().get().getPackageDeclaration().get();
            String qualified = pkg.getNameAsString() + "." + n.getNameAsString();
            return qualified;
        }
    }
    class SystemOutPrintVisitor extends ModifierVisitor<Void> {

        private CompilationUnit compilationUnit;
        
        public SystemOutPrintVisitor(CompilationUnit compilationUnit) {
            super();
            this.compilationUnit = compilationUnit;
        }

        @Override
        public Visitable visit(MethodCallExpr n, Void arg) {
            Optional<Expression> scope = n.getScope();
            // 找出System.out.println();和System.out.print();
            if (scope.isPresent() && "System.out".equals(scope.get().toString())
                    && n.getNameAsString().startsWith("print")) {
                log.info("存在[System.out.println()]代码块，将进行替换");
                ClassOrInterfaceDeclaration parent = n.findAncestor(ClassOrInterfaceDeclaration.class).get();
                addAnnotation(parent, meta);
                addImports(compilationUnit, meta);
                return process(n);
            }
            return super.visit(n, arg);
        }

        private MethodCallExpr process(MethodCallExpr expr) {
            NodeList<Expression> args = expr.getArguments();
            int size = args.size();
            if (size == 0) {
                // System.out.println();是没有意义的，直接删除掉
                return null;
            }
            if (size == 1) {
                Expression arg = args.get(0);
                // 如果判断参数类型不是String的话就应该包装print
                if (!(arg instanceof StringLiteralExpr|| arg instanceof BinaryExpr)) {
                    args.add(0, new StringLiteralExpr("print:{}"));
                }
            }
            String str = "log";
            expr.setScope(new NameExpr(str));
            expr.setName("info");
            return expr;
        }
    }

    class MainMehtodVisitor extends ModifierVisitor<Void> {
        @Override
        public Visitable visit(MethodDeclaration n, Void arg) {
            NodeList<Modifier> set = n.getModifiers();
            String methodName = n.getNameAsString();
            boolean flag = set.contains(Modifier.staticModifier()) && set.contains(Modifier.publicModifier());
            if (flag && "main".equals(methodName)) {
                BlockStmt body = n.getBody().get();
                if (body.getChildNodes().isEmpty()) {
                    log.info("delete empty method:{}", n);
                    // 删除掉main入口方法
                    return null;
                }
            }
            return super.visit(n, arg);
        }
    }

}
