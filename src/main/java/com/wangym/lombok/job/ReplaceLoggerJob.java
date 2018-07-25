package com.wangym.lombok.job;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.wangym.lombok.job.log.LogPackage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author wangym
 * @version 创建时间：2018年5月14日 上午10:30:27
 */
@Component
@Slf4j
public class ReplaceLoggerJob implements Job {

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
        List<FieldDeclaration> logsFieldDecList = extractLoggerFieldDeclaration(compilationUnit);
        LogPackage pkg = getLogPackage(logsFieldDecList);
        if (pkg == null) {
            return;
        }
        List<MethodCallExpr> methodCallList = extractAllMethodCallExpr(compilationUnit);
        List<MethodCallExpr> filterMethodCalls = extractLoggerMethodCallExpr(pkg.getLoggerName(), methodCallList);
        if (filterMethodCalls.isEmpty()) {
            return;
        }
        LexicalPreservingPrinter.setup(compilationUnit);
        renameLoggerRenam(filterMethodCalls);
        // 清除原来的log声明
        pkg.getField().remove();
        addAnnotation(pkg.getClazz());
        deleteImports(compilationUnit);
        String newBody = LexicalPreservingPrinter.print(compilationUnit);
        // 以utf-8编码的方式写入文件中
        FileCopyUtils.copy(newBody.toString().getBytes("utf-8"), file);
    }

    private List<FieldDeclaration> extractLoggerFieldDeclaration(CompilationUnit compilationUnit) {
        List<FieldDeclaration> list = compilationUnit.findAll(FieldDeclaration.class);
        List<FieldDeclaration> result = new ArrayList<>();
        for (FieldDeclaration field : list) {
            List<VariableDeclarator> variableList = field.findAll(VariableDeclarator.class);
            // 假设Logger的声明都是一个变量一个声明，不存在int x=3,y=4;这样的形式
            if (variableList.size() == 1) {
                VariableDeclarator variable = variableList.get(0);
                // 找出关于"Logger"的字段声明
                if ("Logger".equals(variable.getTypeAsString())) {
                    result.add(field);
                }
            }
        }
        return result;
    }

    private List<MethodCallExpr> extractAllMethodCallExpr(CompilationUnit compilationUnit) {
        List<ClassOrInterfaceDeclaration> classList = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
        List<MethodCallExpr> result = new ArrayList<>();
        for (ClassOrInterfaceDeclaration c : classList) {
            result.addAll(c.findAll(MethodCallExpr.class));
        }
        return result;
    }

    private List<MethodCallExpr> extractLoggerMethodCallExpr(String loggerName, List<MethodCallExpr> list) {
        // 提取出是log的方法调用
        List<MethodCallExpr> filter = new ArrayList<>();
        for (MethodCallExpr po : list) {
            if (!po.getScope().isPresent()) {
                continue;
            }
            if (loggerName.equals(po.getScope().get().toString())) {
                filter.add(po);
            }
        }
        return filter;
    }

    private void renameLoggerRenam(List<MethodCallExpr> loggerCall) {
        String str = "log";
        // 对于logger的全部方法调用，替换变量名
        for (MethodCallExpr po : loggerCall) {
            po.setScope(new NameExpr(str));
        }
    }

    private LogPackage getLogPackage(List<FieldDeclaration> loggerFields) {
        // 暂时只能处理一个编译单元最多存在一个Logger的情况
        // 因为如果存在多个，则要判断所属的类作用范围，并且要在正确的类中填充注解，太复杂
        if (loggerFields.size() != 1) {
            return null;
        }
        FieldDeclaration field = loggerFields.get(0);
        // 如果logger的声明不是私有的，不能保证当前编译单元之外的类使用了此变量
        if (!field.isPrivate()) {
            return null;
        }
        List<VariableDeclarator> variableList = field.findAll(VariableDeclarator.class);
        // 假设一个FieldDeclaration只定义一个VariableDeclarator
        VariableDeclarator variable = variableList.get(0);
        Optional<Expression> initializer = variable.getInitializer();
        Expression exp = initializer.get();
        if (exp instanceof MethodCallExpr) {
            String text = ((MethodCallExpr) exp).getArgument(0).toString();
            ClassOrInterfaceDeclaration clazz = getClassName(field);
            String className = clazz.getNameAsString();
            List<String> targetList = Arrays.asList(className, "getClass");
            for (String target : targetList) {
                boolean check = text.contains(target);
                if (check) {
                    return new LogPackage(field, variable.getNameAsString(), clazz);
                }
            }
        }
        return null;
    }

    private ClassOrInterfaceDeclaration getClassName(FieldDeclaration field) {
        Node node = field.getParentNode().get();
        if (node instanceof ClassOrInterfaceDeclaration) {
            ClassOrInterfaceDeclaration c = (ClassOrInterfaceDeclaration) node;
            return c;
        }
        throw new RuntimeException("未期待的异常");
    }

    private void addAnnotation(ClassOrInterfaceDeclaration c) {
        NodeList<AnnotationExpr> anns = c.getAnnotations();
        String name = "Slf4j";
        boolean notExist = anns.stream()
                .filter(it -> name.equals(it.getNameAsString()))
                .count() == 0;
        if (notExist) {
            anns.add(new MarkerAnnotationExpr(name));
        }
    }

    private void deleteImports(CompilationUnit compilationUnit) {
        NodeList<ImportDeclaration> imports = compilationUnit.getImports();
        List<String> deleteImports = Arrays.asList(
                "org.slf4j.Logger",
                "org.slf4j.LoggerFactory",
                "org.apache.log4j.Logger");
        imports.stream()
                .filter(it -> {
                    return deleteImports.contains(it.getName().asString());
                })
                // 不可边循环边删除,所以先filter出一个集合再删除
                .collect(Collectors.toList())
                .forEach(it -> compilationUnit.remove(it));
        String str = "lombok.extern.slf4j.Slf4j";
        boolean notExist = imports.stream()
                .filter(it -> str.equals(it.getName().asString()))
                .count() == 0;
        if (notExist) {
            imports.add(new ImportDeclaration(str, false, false));
        }
    }

}
