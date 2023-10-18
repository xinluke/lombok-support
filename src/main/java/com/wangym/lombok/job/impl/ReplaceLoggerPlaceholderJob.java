package com.wangym.lombok.job.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.wangym.lombok.job.AbstractJavaJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author wangym
 * @version 创建时间：2018年5月14日 上午10:30:27
 */
@Component
@Slf4j
public class ReplaceLoggerPlaceholderJob extends AbstractJavaJob {

    @Override
    public void process(CompilationUnit compilationUnit) {
        boolean hasAnn = compilationUnit.findAll(ClassOrInterfaceDeclaration.class).stream().filter(this::isTarget)
                .count() > 0;
        if (!hasAnn) {
            return;
        }
        LoggerPlaceholderVisitor visitor = new LoggerPlaceholderVisitor();
        compilationUnit.accept(visitor, null);
    }

    private boolean isTarget(ClassOrInterfaceDeclaration c) {
        NodeList<AnnotationExpr> anns = c.getAnnotations();
        String name = "Slf4j";
        boolean notExist = anns.stream()
                .filter(it -> name.equals(it.getNameAsString()))
                .count() == 0;
        return !notExist;
    }

    @Override
    public int getOrder() {
        // 更改任务的优先级
        return Ordered.LOWEST_PRECEDENCE;
    }

    class LoggerPlaceholderVisitor extends ModifierVisitor<Void> {
        private SimpleName ErrorName = new SimpleName("error");
        private StringLiteralExpr emptyContentExpr = new StringLiteralExpr("");

        @Override
        public Visitable visit(MethodCallExpr n, Void arg) {
            if (!n.getScope().isPresent()) {
                return super.visit(n, arg);
            }
            String methodCallStr = n.toString();
            // 如果是判断类型的方法则跳过
            if (methodCallStr.startsWith("log.is")) {
                return super.visit(n, arg);
            }
            if ("log".equals(n.getScope().get().toString())) {
                NodeList<Expression> args = n.getArguments();
                Expression expr = args.get(0);
                // log.info方法的第一个参数必然为字符串，如果不是字符串，说明可以转化为字符串占位符的表现形式
                if (expr instanceof BinaryExpr) {
                    try {
                        return doHandle(n);
                    } catch (Exception e) {
                        log.error("处理失败", e);
                    }
                }else if(expr instanceof MethodCallExpr){
                    doHandle1(n);
                }
                // 去除参数中的"JSON.toJSONString(list)"
                if (ruleConf.isDeleteJsonWraped() && args.size() > 1) {
                    for (ListIterator<Expression> iterator = args.listIterator(); iterator.hasNext();) {
                        Expression itExpr = iterator.next();
                        if (itExpr instanceof MethodCallExpr) {
                            MethodCallExpr mc = (MethodCallExpr) itExpr;
                            NodeList<Expression> res = getExtract(mc);
                            if (res != null) {
                                iterator.remove();
                            }
                            // 把mc中的列表加回来
                            res.forEach(iterator::add);
                        }
                    }
                }
                // 如果出现log.error("",e)这样类似的语句，第一个参数添加打印内容
                if (emptyContentExpr.equals(expr) && ErrorName.equals(n.getName())) {
                    // 替换第一个参数
                    args.set(0, new StringLiteralExpr(Constant.DEFAULT_ERROR_MSG));
                }
            }
            return super.visit(n, arg);
        }

        private NodeList<Expression> getExtract(MethodCallExpr mc) {
            if (Optional.of(new NameExpr("JSON")).equals(mc.getScope())
                    && new SimpleName("toJSONString").equals(mc.getName())) {
                // 从调用的表达式中提取出参数列表
                return mc.getArguments();
            }
            return null;
        }

        private void doHandle1(MethodCallExpr po) {
            NodeList<Expression> args = po.getArguments();
            Expression expr = args.get(0);
            // 如果第一个参数是String.format("tmp=%s", "123")的表现形式
            if (expr instanceof MethodCallExpr) {
                if (!expr.toString().startsWith("String.format(")) {
                    return;
                }
                NodeList<Expression> arglist = ((MethodCallExpr) expr).getArguments();
                Expression node = arglist.get(0);
                // 如果第一个参数值不为字符串的形式的话，说明是可以转化的
                if (!(node instanceof StringLiteralExpr)) {
                    return;
                }
                String string = ((StringLiteralExpr) node).getValue();
                int key = count(string);
                // 如果替换的参数变量不等于参数的个数，则不执行
                if (key != arglist.size() - 1) {
                    return;
                }
                args.remove(0);
                args.add(0, new StringLiteralExpr(processMsg(string)));
                // 从第二个参数开始添加
                List<Expression> newArgs = new ArrayList<>();
                for (int i = 1; i < arglist.size(); i++) {
                    newArgs.add(arglist.get(i));
                }
                args.addAll(1, newArgs);
            }
        }

        private int count(String string) {
            List<String> keys = Arrays.asList("%s", "%d");
            int count = 0;
            for (String key : keys) {
                count += StringUtils.countMatches(string, key);
            }
            return count;
        }

        private String processMsg(String string) {
            return string.replaceAll("%s", "{}").replaceAll("%d", "{}");
        }

        private MethodCallExpr doHandle(MethodCallExpr po) {
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
            return po;
        }

        private List<Expression> searchExpression(Expression expression) {
            if (!(expression instanceof BinaryExpr)) {
                return Arrays.asList(expression);
            } else {
                BinaryExpr expr = (BinaryExpr) expression;
                // 判断是否是用'+'连接的字符串表达式
                if (expr.getOperator() != BinaryExpr.Operator.PLUS) {
                    // 如果不是连加的数据表达式，单独作为一项
                    return Arrays.asList(expr);
                } else {
                    List<Expression> list = new ArrayList<>();
                    list.addAll(searchExpression(expr.getLeft()));
                    list.addAll(searchExpression(expr.getRight()));
                    return list;
                }
            }
        }
    }

}
