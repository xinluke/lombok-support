package com.wangym.lombok.job;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wangym
 * @version 创建时间：2018年8月4日 下午5:33:47
 */
public abstract class AbstractJob implements Job {
    private String suffix;

    @Override
    public boolean canRead(String fileName) {
        if (fileName.endsWith(suffix)) {
            return true;
        } else {
            return false;
        }
    }

    protected void addAnnotation(ClassOrInterfaceDeclaration c, Metadata meta) {
        NodeList<AnnotationExpr> anns = c.getAnnotations();
        String name = meta.getAnnName();
        boolean notExist = anns.stream()
                .filter(it -> name.equals(it.getNameAsString()))
                .count() == 0;
        if (notExist) {
            anns.add(new MarkerAnnotationExpr(name));
        }
    }

    protected void deleteImports(CompilationUnit compilationUnit, Metadata meta) {
        NodeList<ImportDeclaration> imports = compilationUnit.getImports();
        String str = meta.getImportPkg();
        boolean exist = imports.stream()
                .filter(it -> {
                    String asString = it.getName().asString();
                    // 普通模式的比较
                    return str.equals(asString);
                })
                .count() > 0;
        if (exist) {
            imports.remove(new ImportDeclaration(str, false, false));
        }
    }

    protected void deleteImports(CompilationUnit compilationUnit, List<String> deleteImports) {
        NodeList<ImportDeclaration> imports = compilationUnit.getImports();
        imports.stream()
                .filter(it -> {
                    return deleteImports.contains(it.getName().asString());
                })
                // 不可边循环边删除,所以先filter出一个集合再删除
                .collect(Collectors.toList())
                .forEach(it -> compilationUnit.remove(it));
    }

    protected void replaceImportsIfExist(CompilationUnit compilationUnit, Metadata oldMeta, Metadata newMeta) {
        List<String> deleteImports= Arrays.asList(oldMeta.getImportPkg());
        NodeList<ImportDeclaration> imports = compilationUnit.getImports();
        List<ImportDeclaration> res = imports.stream()
                .filter(it -> {
                    return deleteImports.contains(it.getName().asString());
                })
                // 不可边循环边删除,所以先filter出一个集合再删除
                .collect(Collectors.toList());
        if (!res.isEmpty()) {
            res.stream()
                    .forEach(it -> compilationUnit.remove(it));
        }
        //添加和删除独立
        addImports(compilationUnit, newMeta);

    }

    protected void addImports(CompilationUnit compilationUnit, Metadata meta) {
        NodeList<ImportDeclaration> imports = compilationUnit.getImports();
        String str = meta.getImportPkg();
        // 上一级的import路径
        String preFix = str.substring(0, str.lastIndexOf("."));
        boolean notExist = imports.stream()
                .filter(it -> {
                    String asString = it.getName().asString();
                    // 使用*只能引入当前包下所有的类，不包含它包含的子包的类
                    // 判断是否是符合带星号的import方式
                    if (it.isAsterisk()) {
                        return asString.equals(preFix);
                    } else {
                        // 普通模式的比较
                        return str.equals(asString);
                    }
                })
                .count() == 0;
        if (notExist) {
            imports.add(new ImportDeclaration(str, false, false));
        }
    }

    public AbstractJob(String suffix) {
        super();
        this.suffix = suffix;
    }

}
