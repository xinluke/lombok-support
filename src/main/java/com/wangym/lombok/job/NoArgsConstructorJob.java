package com.wangym.lombok.job;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author wangym
 * @version 创建时间：2018年12月27日 上午10:48:35
 */
@Component
@Slf4j
public class NoArgsConstructorJob extends JavaJob {

    private Metadata meta = new Metadata("NoArgsConstructor", "lombok.NoArgsConstructor");

    @Override
    public void handle(File file) throws IOException {
        byte[] bytes = FileCopyUtils.copyToByteArray(file);
        CompilationUnit compilationUnit = JavaParser.parse(new String(bytes, "utf-8"));
        NoArgsConstructorVisitor visitor = new NoArgsConstructorVisitor();
        compilationUnit.clone().accept(visitor, null);
        if (visitor.isModify()) {
            LexicalPreservingPrinter.setup(compilationUnit);
            compilationUnit.accept(visitor, null);
            addImports(compilationUnit, meta);
            String newBody = LexicalPreservingPrinter.print(compilationUnit);
            // 以utf-8编码的方式写入文件中
            FileCopyUtils.copy(newBody.toString().getBytes("utf-8"), file);
        }
    }

    @Getter
    class NoArgsConstructorVisitor extends ModifierVisitor<Void> {
        private boolean modify = false;

        @Override
        public Visitable visit(ConstructorDeclaration n, Void arg) {
            if (n.getModifiers().contains(Modifier.PUBLIC)) {
                String nameAsString = n.getNameAsString();
                log.debug("读取到无参构造方法：{}", nameAsString);
                String body = n.getBody().toString().trim();
                // 去除语法块中的'{'和'}'字符
                body = body.substring(1, body.length() - 1);
                // 去除不可见的字符
                body = StringUtils.strip(body);
                // 如果是没有内容的话，是可以替换成@NoArgsConstructor注解的形式的
                if (StringUtils.isBlank(body)) {
                    log.info("替换无参构造方法：{}", nameAsString);
                    // 设置标志位
                    modify = true;
                    ClassOrInterfaceDeclaration parent = n.findParent(ClassOrInterfaceDeclaration.class).get();
                    addAnnotation(parent, meta);
                    return null;
                }
            }
            return super.visit(n, arg);
        }

    }

}
