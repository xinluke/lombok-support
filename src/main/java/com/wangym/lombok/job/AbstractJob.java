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
        boolean notExist = imports.stream()
                .filter(it -> str.equals(it.getName().asString()))
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
