package com.wangym.lombok.job.impl.migration;

import com.github.javaparser.ast.expr.Name;
import com.wangym.lombok.job.Metadata;
import lombok.Getter;

/**
 * @Author: wangym
 * @Date: 2022/7/21 10:15
 */
@Getter
public class AnnotationMetaModel {
    //匹配的名称
    private Name annName;
    //需要替换的名称
    private Name newAnnName;
    private Metadata importPackage;
    private Metadata newImportPackage;

    public AnnotationMetaModel(String annName, String importPackage, String newImportPackage) {
        this.annName = new Name(annName);
        this.newAnnName = new Name(annName);
        this.importPackage = new Metadata(annName, importPackage);
        this.newImportPackage = new Metadata(annName, newImportPackage);
    }

    public AnnotationMetaModel(String annName, String newAnnName, String importPackage, String newImportPackage) {
        this.annName = new Name(annName);
        this.newAnnName = new Name(newAnnName);
        this.importPackage = new Metadata(annName, importPackage);
        this.newImportPackage = new Metadata(newAnnName, newImportPackage);
    }
}
