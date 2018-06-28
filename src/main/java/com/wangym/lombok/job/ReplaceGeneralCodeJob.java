package com.wangym.lombok.job;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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
public class ReplaceGeneralCodeJob implements Job {

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
        List<String> classNames = compilationUnit.findAll(ClassOrInterfaceDeclaration.class).stream()
                .filter(c -> !c.isInterface())
                .filter(this::test)
                .map(ClassOrInterfaceDeclaration::getNameAsString)
                .collect(Collectors.toList());
        if (!classNames.isEmpty()) {
            LexicalPreservingPrinter.setup(compilationUnit);
            // 找出符合条件的class进行处理
            compilationUnit.findAll(ClassOrInterfaceDeclaration.class).stream()
                    .filter(c -> classNames.contains(c.getNameAsString()))
                    .forEach(c -> {
                        c.getMethods().stream()
                                .forEach(it -> c.remove(it));
                        addAnnotation(c);
                    });
            log.info("当前文件符合转换,class name:{}", file.getName());
            addImports(compilationUnit);
            String newBody = LexicalPreservingPrinter.print(compilationUnit);
            // 暂时使用正则表达式的方式修正格式错误的问题
            newBody = newBody.replaceAll(";import", ";\n\nimport");
            // 以utf-8编码的方式写入文件中
            FileCopyUtils.copy(newBody.toString().getBytes("utf-8"), file);
        }
    }

    private boolean test(ClassOrInterfaceDeclaration clazz) {
        ClassOrInterfaceDeclaration c = clazz.clone();
        List<FieldDeclaration> fields = c.getFields();
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
            return count == 0;
        }
        return false;
    }

    private void addAnnotation(ClassOrInterfaceDeclaration c) {
        NodeList<AnnotationExpr> anns = c.getAnnotations();
        String name = "Data";
        boolean notExist = anns.stream()
                .filter(it -> name.equals(it.getNameAsString()))
                .count() == 0;
        if (notExist) {
            anns.add(new MarkerAnnotationExpr(name));
        }
    }

    private void addImports(CompilationUnit compilationUnit) {
        NodeList<ImportDeclaration> imports = compilationUnit.getImports();
        String str = "lombok.Data";
        boolean notExist = imports.stream()
                .filter(it->str.equals(it.getName().asString()))
                .count() == 0;
        if (notExist) {
            imports.add(new ImportDeclaration(str, false, false));
        }
    }

    private MethodDeclaration createGetter(FieldDeclaration field) {
        if (field.getVariables().size() != 1) {
            throw new IllegalStateException("You can use this only when the field declares only 1 variable name");
        }
        Optional<ClassOrInterfaceDeclaration> parentClass = field.getAncestorOfType(ClassOrInterfaceDeclaration.class);
        Optional<EnumDeclaration> parentEnum = field.getAncestorOfType(EnumDeclaration.class);
        if (!(parentClass.isPresent() || parentEnum.isPresent())
                || (parentClass.isPresent() && parentClass.get().isInterface())) {
            throw new IllegalStateException("You can use this only when the field is attached to a class or an enum");
        }
        VariableDeclarator variable = field.getVariable(0);
        String fieldName = variable.getNameAsString();
        String fieldNameUpper = fieldName.toUpperCase().substring(0, 1) + fieldName.substring(1, fieldName.length());
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
