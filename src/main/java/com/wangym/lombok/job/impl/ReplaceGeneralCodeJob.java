package com.wangym.lombok.job.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.wangym.lombok.job.AbstractJavaJob;
import com.wangym.lombok.job.Metadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.javaparser.ast.Modifier.PUBLIC;

/**
 * @author wangym
 * @version 创建时间：2018年6月7日 上午9:42:59
 */
@Component
@Slf4j
public class ReplaceGeneralCodeJob extends AbstractJavaJob {
    private Metadata meta = new Metadata("Data", "lombok.Data");

    @Override
    public void process(CompilationUnit compilationUnit) {
        int before = compilationUnit.hashCode();
        GenernalCodeVisitor visitor = new GenernalCodeVisitor();
        compilationUnit.accept(visitor, null);
        // 如果存在变更，则操作
        if (before != compilationUnit.hashCode()) {
            addImports(compilationUnit, meta);
        }
    }

    class GenernalCodeVisitor extends ModifierVisitor<Void> {
        private boolean isTemplateCode;

        @Override
        public Visitable visit(ClassOrInterfaceDeclaration n, Void arg) {
            ClassOrInterfaceDeclaration c = n.clone();
            List<FieldDeclaration> fields = c.getFields().stream()
                    // 排除静态的字段
                    .filter(it -> {
                        EnumSet<Modifier> sets = it.getModifiers();
                        return !sets.contains(Modifier.STATIC);
                    })
                    .collect(Collectors.toList());
            List<MethodDeclaration> methods = c.getMethods();
            if (!fields.isEmpty() && (fields.size() * 2 == methods.size())) {
                List<String> virtualMethods = fields.stream()
                        .map(it -> {
                            MethodDeclaration getter = createGetter(it);
                            return Arrays.asList(getter.toString(), it.createSetter().toString());
                        })
                        .flatMap(List::stream)
                        .collect(Collectors.toList());
                // 验证全部的方法是否都是getter和setter
                long count = methods.stream()
                        .filter(it -> {
                            return !virtualMethods.contains(it.toString());
                        })
                        .count();
                isTemplateCode = count == 0;
            }
            if (isTemplateCode) {
                addAnnotation(n, meta);
            }
            return super.visit(n, arg);
        }

        @Override
        public Visitable visit(MethodDeclaration n, Void arg) {
            // 如果是符合条件的类则把写的getter/setter模板代码删除
            if (isTemplateCode) {
                return null;
            } else {
                return super.visit(n, arg);
            }
        }

        private MethodDeclaration createGetter(FieldDeclaration field) {
            if (field.getVariables().size() != 1) {
                throw new IllegalStateException("You can use this only when the field declares only 1 variable name");
            }
            Optional<ClassOrInterfaceDeclaration> parentClass = field
                    .getAncestorOfType(ClassOrInterfaceDeclaration.class);
            Optional<EnumDeclaration> parentEnum = field.getAncestorOfType(EnumDeclaration.class);
            if (!(parentClass.isPresent() || parentEnum.isPresent())
                    || (parentClass.isPresent() && parentClass.get().isInterface())) {
                throw new IllegalStateException(
                        "You can use this only when the field is attached to a class or an enum");
            }
            VariableDeclarator variable = field.getVariable(0);
            String fieldName = variable.getNameAsString();
            String fieldNameUpper = fieldName.toUpperCase().substring(0, 1)
                    + fieldName.substring(1, fieldName.length());
            String finalFieldNameUpper;
            // 如果是boolean类型的字段，生成isXXX类型的方法
            if (variable.getType().equals(PrimitiveType.booleanType())) {
                finalFieldNameUpper = "is" + fieldNameUpper;
            } else {
                finalFieldNameUpper = "get" + fieldNameUpper;
            }
            final MethodDeclaration getter;
            getter = parentClass.map(clazz -> clazz.addMethod(finalFieldNameUpper, PUBLIC))
                    .orElseGet(() -> parentEnum.get().addMethod(finalFieldNameUpper, PUBLIC));
            getter.setType(variable.getType());
            BlockStmt blockStmt = new BlockStmt();
            getter.setBody(blockStmt);
            blockStmt.addStatement(new ReturnStmt(fieldName));
            return getter;
        }
    }
}
