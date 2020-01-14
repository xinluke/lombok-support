package com.wangym.lombok.job;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;

public abstract class AbstractJavaJob extends JavaJob {

    @Override
    public void handle(File file) throws IOException {
        byte[] bytes = FileCopyUtils.copyToByteArray(file);
        String code = new String(bytes, "utf-8");
        CompilationUnit compilationUnit = JavaParser.parse(code);
        int before = compilationUnit.hashCode();
        CompilationUnit clone = compilationUnit.clone();
        // 进行预操作
        process(clone);
        // 如果存在变更，则操作
        if (before != clone.hashCode()) {
            // 使用词法打印机设置对象，以便于保存原本的语法格式
            LexicalPreservingPrinter.setup(compilationUnit);
            // 操作真正的对象
            process(compilationUnit);
            String newBody = LexicalPreservingPrinter.print(compilationUnit);
            // 以utf-8编码的方式写入文件中
            FileCopyUtils.copy(newBody.toString().getBytes("utf-8"), file);
        }
    }

    public abstract void process(CompilationUnit compilationUnit);

}
