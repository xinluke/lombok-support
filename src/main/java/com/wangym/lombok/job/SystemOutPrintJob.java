package com.wangym.lombok.job;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * @author wangym
 * @version 创建时间：2018年11月6日 下午2:27:46
 */
@Component
@Slf4j
public class SystemOutPrintJob extends JavaJob {
    private Metadata meta = new Metadata("Slf4j", "lombok.extern.slf4j.Slf4j");

    @Override
    public void handle(File file) throws IOException {
        byte[] bytes = FileCopyUtils.copyToByteArray(file);
        CompilationUnit compilationUnit = JavaParser.parse(new String(bytes, "utf-8"));
        SystemOutPrintVisitor visitor = new SystemOutPrintVisitor();
        compilationUnit.clone().accept(visitor, null);
        if (visitor.isModify()) {
            log.info("存在[System.out.println()]代码块，将进行替换");
            LexicalPreservingPrinter.setup(compilationUnit);
            compilationUnit.accept(visitor, null);
            addImports(compilationUnit, meta);
            String newBody = LexicalPreservingPrinter.print(compilationUnit);
            // 以utf-8编码的方式写入文件中
            FileCopyUtils.copy(newBody.toString().getBytes("utf-8"), file);
        }
    }

    @Getter
    class SystemOutPrintVisitor extends ModifierVisitor<Void> {
        private boolean modify = false;

        @Override
        public Visitable visit(MethodCallExpr n, Void arg) {
            Optional<Expression> scope = n.getScope();
            if (scope.isPresent() && "System.out".equals(scope.get().toString())
                    && n.getNameAsString().startsWith("print")) {
                modify = true;
                ClassOrInterfaceDeclaration parent = n.findParent(ClassOrInterfaceDeclaration.class).get();
                addAnnotation(parent, meta);
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
                // 如果是一个变量
                if (arg instanceof NameExpr) {
                    args.add(0, new StringLiteralExpr("print:{}"));
                }
            }
            String str = "log";
            expr.setScope(new NameExpr(str));
            expr.setName("info");
            return expr;
        }
    }

}
