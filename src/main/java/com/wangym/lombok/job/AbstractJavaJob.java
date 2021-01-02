package com.wangym.lombok.job;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;

@Slf4j
public abstract class AbstractJavaJob extends JavaJob {

    @Setter
    private boolean showDetail;

    @Override
    public void handle(File file) throws IOException {
        byte[] bytes = FileCopyUtils.copyToByteArray(file);
        String code = new String(bytes, "utf-8");
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        int before = compilationUnit.hashCode();
        CompilationUnit clone = compilationUnit.clone();
        CompilationUnit clone2 = compilationUnit.clone();
        // 进行预操作
        process(clone);
        applyPreProcess(compilationUnit.clone(), file.getPath());
        // 如果存在变更，则操作
        if (before != clone.hashCode()) {
            // 使用词法打印机设置对象，以便于保存原本的语法格式
            LexicalPreservingPrinter.setup(compilationUnit);
            // 操作真正的对象
            process(compilationUnit);
            int recordCode = clone2.hashCode();
            String fileName = afterProcess(clone2);
            String newBody = LexicalPreservingPrinter.print(compilationUnit);
            if (showDetail) {
                log.info("文件处理成功\n{}", newBody);
            }

            // 以utf-8编码的方式写入文件中
            FileCopyUtils.copy(newBody.toString().getBytes("utf-8"), file);
            if (recordCode != clone.hashCode()) {
                String pathname = file.getParent() + "/" + fileName;
                log.debug("find change,file:{},content:{}", pathname, clone2);
                FileCopyUtils.copy(clone2.toString().getBytes("utf-8"), new File(pathname));
            }
            log.info("文件处理完成");
        }
    }

    public abstract void process(CompilationUnit compilationUnit);
    
    public void applyPreProcess(CompilationUnit compilationUnit, String path) {

    }
    public String afterProcess(CompilationUnit compilationUnit) {
        // 空实现
        return null;
    }

}
