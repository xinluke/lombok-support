package com.wangym.lombok.job.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.AssignExpr.Operator;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.wangym.lombok.job.AbstractJavaJob;
import com.wangym.lombok.job.Metadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
                        NodeList<Modifier> sets = it.getModifiers();
                        return !sets.contains(Modifier.staticModifier());
                    })
                    .collect(Collectors.toList());
            List<MethodDeclaration> methods = c.getMethods();
            if (!fields.isEmpty() && (fields.size() * 2 == methods.size())) {
                log.debug("find posible GenernalCodeClass:{}", c.getName());
                List<String> virtualMethods = fields.stream()
                        .map(it -> {
                            MethodDeclaration getter = createGetter(it);
                            return Arrays.asList(getter.toString(), createSetter(it).toString());
                        })
                        .flatMap(List::stream)
                        .collect(Collectors.toList());
                //log.debug("virtualMethods:{}", virtualMethods);
                // 验证全部的方法是否都是getter和setter
                long count = methods.stream()
                        .filter(it -> {
                            boolean contains = virtualMethods.contains(it.toString());
                            if (!contains) {
                                log.debug("not target method:{}", it);
                            }
                            return !contains;
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
                    .findAncestor(ClassOrInterfaceDeclaration.class);
            Optional<EnumDeclaration> parentEnum = field.findAncestor(EnumDeclaration.class);
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
                //如果是is开头的，则不再处理
                if (fieldName.startsWith("is")) {
                    finalFieldNameUpper = fieldName;
                } else {
                    finalFieldNameUpper = "is" + fieldNameUpper;
                }
            } else {
                //正常的情况，则方法名第一个字符就是大写
                if(testNormalFieldName(fieldName)) {
                    finalFieldNameUpper = "get" + fieldNameUpper;
                } else {
                    finalFieldNameUpper = "get" + fieldName;
                }
            }
            final MethodDeclaration getter;
            getter = parentClass.map(clazz -> clazz.addMethod(finalFieldNameUpper, Keyword.PUBLIC))
                    .orElseGet(() -> parentEnum.get().addMethod(finalFieldNameUpper, Keyword.PUBLIC));
            getter.setType(variable.getType());
            BlockStmt blockStmt = new BlockStmt();
            getter.setBody(blockStmt);
            blockStmt.addStatement(new ReturnStmt(fieldName));
            return getter;
        }
        private MethodDeclaration createSetter(FieldDeclaration field) {
            if (field.getVariables().size() != 1) {
                throw new IllegalStateException("You can use this only when the field declares only 1 variable name");
            }
            Optional<ClassOrInterfaceDeclaration> parentClass = field.findAncestor(ClassOrInterfaceDeclaration.class);
            Optional<EnumDeclaration> parentEnum = field.findAncestor(EnumDeclaration.class);
            if (!(parentClass.isPresent() || parentEnum.isPresent()) || (parentClass.isPresent() && parentClass.get().isInterface())) {
                throw new IllegalStateException("You can use this only when the field is attached to a class or an enum");
            }
            VariableDeclarator variable = field.getVariable(0);
            // 这个是原始的值
            String fieldNameOld = variable.getNameAsString();
            // 目标的字段类型，有可能入参和其他地方处理需要这个处理后的字段，初始是一样的
            String fieldName = fieldNameOld;
            // 如果是一个布尔类型的，is开头的驼峰词组，则切掉is，并且将第一位小写
            if (variable.getType().equals(PrimitiveType.booleanType()) 
                    && fieldName.startsWith("is")
                    && !fieldName.equals(fieldName.toLowerCase())) {
                fieldName = fieldName.substring(2, 3).toLowerCase() + fieldName.substring(3);
            }
            String fieldNameUpper = fieldName.toUpperCase().substring(0, 1) + fieldName.substring(1, fieldName.length());
            String finalFieldNameUpper;
            // 正常的情况，则方法名第一个字符就是大写
            if (testNormalFieldName(fieldName)) {
                finalFieldNameUpper = "set" + fieldNameUpper;
            } else {
                finalFieldNameUpper = "set" + fieldName;
            }
            final MethodDeclaration setter;
            setter = parentClass.map(clazz -> clazz.addMethod(finalFieldNameUpper, Modifier.Keyword.PUBLIC))
                    .orElseGet(() -> parentEnum.get().addMethod(finalFieldNameUpper, Modifier.Keyword.PUBLIC));
            setter.setType(new VoidType());
            setter.getParameters().add(new Parameter(variable.getType(), fieldName));
            BlockStmt blockStmt2 = new BlockStmt();
            setter.setBody(blockStmt2);
            blockStmt2.addStatement(new AssignExpr(getTargetExpr(fieldNameOld, fieldName), new NameExpr(fieldName), Operator.ASSIGN));
            return setter;
        }

        private NameExpr getTargetExpr(String fieldNameOld, String fieldName) {
            // idea模板生成的set方法，如果是boolean值，并且字段是"is"驼峰词组，去除过，则不拼接this，感觉像是idea的bug，兼容处理
            // 例如这种
//            public void setSuccess(boolean success) {
//                isSuccess = success;
//            }
            //
            if (!fieldNameOld.equals(fieldName)) {
                return new NameExpr(fieldNameOld);
            } else {
                // 通常情况直接返回
                return new NameExpr("this." + fieldNameOld);
            }
        }

        private boolean testNormalFieldName(String name) {
            // 单字符的情况
            if (name.length() < 2) {
                return true;
            }
            String substring = name.substring(0, 2);
            // 判断前两个字符是否是全小写，如果都是全小写，认为是异常的情况
            return substring.equals(substring.toLowerCase());
        }
    }
}
