package com.wangym.lombok.job;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wangym
 * @version 创建时间：2018年5月14日 上午10:30:27
 */
@Component
@Slf4j
public class ReplaceLoggerPlaceholderJob implements Job {

    @Override
    public boolean canRead(String fileName) {
        if (fileName.endsWith(".java")) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void handle(File file) throws IOException {
        byte[] bytes = FileCopyUtils.copyToByteArray(file);
        CompilationUnit compilationUnit = JavaParser.parse(new String(bytes, "utf-8"));
        List<MethodCallExpr> list = extractAllMethodCallExpr(compilationUnit);
        List<MethodCallExpr> filter = extractLoggerMethodCallExpr(list);
        if (filter.isEmpty()) {
            return;
        }
        LexicalPreservingPrinter.setup(compilationUnit);
        for (MethodCallExpr po : filter) {
            try {
                doHandle(po);
            } catch (Exception e) {
                log.error("处理失败:{}", po);
            }
        }
        String newBody = LexicalPreservingPrinter.print(compilationUnit);
        // 以utf-8编码的方式写入文件中
        FileCopyUtils.copy(newBody.toString().getBytes("utf-8"), file);
    }

    private void doHandle(MethodCallExpr po) {
        NodeList<Expression> args = po.getArguments();
        Expression expr = args.get(0);
        // 如果是一个表达式
        if (expr instanceof BinaryExpr) {
            List<Expression> ll = searchExpression(expr);
            List<Expression> extract = new ArrayList<>();
            StringBuffer sb = new StringBuffer();
            for (Expression it : ll) {
                if (it instanceof StringLiteralExpr) {
                    StringLiteralExpr stringLiteralExpr = (StringLiteralExpr) it;
                    String asString = stringLiteralExpr.getValue();
                    sb.append(asString);
                } else {
                    // 标准占位符
                    sb.append("{}");
                    extract.add(it);
                }
            }
            StringLiteralExpr format = new StringLiteralExpr(sb.toString());
            args.remove(0);
            args.add(0, format);
            // 跟在第一位插入，最先插入的排最后
            for (int i = extract.size() - 1; i >= 0; i--) {
                Expression nameExpr = extract.get(i);
                args.addAfter(nameExpr, format);
            }
        }
    }

    private List<MethodCallExpr> extractAllMethodCallExpr(CompilationUnit compilationUnit) {
        List<ClassOrInterfaceDeclaration> classList = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
        List<MethodCallExpr> result = new ArrayList<>();
        boolean hasAnno = false;
        for (ClassOrInterfaceDeclaration c : classList) {
            if (isTarget(c)) {
                hasAnno = true;
            }
            if (hasAnno) {
                result.addAll(c.findAll(MethodCallExpr.class));
            }
        }
        return result;
    }

    private List<MethodCallExpr> extractLoggerMethodCallExpr(List<MethodCallExpr> list) {
        // 提取出是log的方法调用
        List<MethodCallExpr> filter = new ArrayList<>();
        for (MethodCallExpr po : list) {
            if (!po.getScope().isPresent()) {
                continue;
            }
            if ("log".equals(po.getScope().get().toString())) {
                NodeList<Expression> args = po.getArguments();
                Expression expr = args.get(0);
                if (expr instanceof BinaryExpr) {
                    filter.add(po);
                }
            }
        }
        return filter;
    }

    private List<Expression> searchExpression(Expression expression) {
        List<Expression> list = new ArrayList<>();
        if (expression instanceof BinaryExpr) {
            BinaryExpr expr = (BinaryExpr) expression;
            if (expr.getOperator() != BinaryExpr.Operator.PLUS) {
                // 暂时只能支持连加的数据
                throw new RuntimeException("unsupport operator");
            }
            list.addAll(searchExpression(expr.getLeft()));
            list.addAll(searchExpression(expr.getRight()));
        } else {
            // 假设这个表达式应该由这些部分构成
            if (!(expression instanceof NameExpr) && !(expression instanceof StringLiteralExpr)
                    && !(expression instanceof MethodCallExpr) && !(expression instanceof FieldAccessExpr)
                    && !(expression instanceof EnclosedExpr)) {
                throw new RuntimeException("unsupport expression");
            }
            list.add(expression);
            return list;
        }
        return list;
    }

    private boolean isTarget(ClassOrInterfaceDeclaration c) {
        NodeList<AnnotationExpr> anns = c.getAnnotations();
        String name = "Slf4j";
        boolean notExist = anns.stream()
                .filter(it -> name.equals(it.getNameAsString()))
                .count() == 0;
        return !notExist;
    }

}
