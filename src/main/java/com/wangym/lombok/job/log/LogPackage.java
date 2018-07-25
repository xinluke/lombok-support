package com.wangym.lombok.job.log;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import lombok.Getter;

/**
 * @author wangym
 * @version 创建时间：2018年6月4日 下午4:00:39
 */
@Getter
public class LogPackage {

    private FieldDeclaration field;
    private String loggerName;
    private ClassOrInterfaceDeclaration clazz;

    public LogPackage(FieldDeclaration field, String loggerName, ClassOrInterfaceDeclaration clazz) {
        super();
        this.field = field;
        this.loggerName = loggerName;
        this.clazz = clazz;
    }

}
