package com.wangym.lombok.job;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;

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
