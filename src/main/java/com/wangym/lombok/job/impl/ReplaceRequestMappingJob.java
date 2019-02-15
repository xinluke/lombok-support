package com.wangym.lombok.job.impl;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.wangym.lombok.job.JavaJob;
import com.wangym.lombok.job.Metadata;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wangym
 * @version 创建时间：2018年6月7日 上午9:42:59
 */
@Component
@Slf4j
public class ReplaceRequestMappingJob extends JavaJob {

    private static Map<String, Metadata> mapping = new HashMap<>();
    static {
        mapping.put("RequestMethod.GET",
                new Metadata("GetMapping", "org.springframework.web.bind.annotation.GetMapping"));
        mapping.put("RequestMethod.POST",
                new Metadata("PostMapping", "org.springframework.web.bind.annotation.PostMapping"));
        mapping.put("RequestMethod.PUT",
                new Metadata("PutMapping", "org.springframework.web.bind.annotation.PutMapping"));
        mapping.put("RequestMethod.DELETE",
                new Metadata("DeleteMapping", "org.springframework.web.bind.annotation.DeleteMapping"));
        mapping.put("RequestMethod.PATCH",
                new Metadata("PatchMapping", "org.springframework.web.bind.annotation.PatchMapping"));
    }

    @Override
    public void handle(File file) throws IOException {
        byte[] bytes = FileCopyUtils.copyToByteArray(file);
        CompilationUnit compilationUnit = JavaParser.parse(new String(bytes, "utf-8"));
        RequestMappingConstructorVisitor visitor = new RequestMappingConstructorVisitor();
        compilationUnit.clone().accept(visitor, null);
        if (visitor.isModify()) {
            log.info("存在[@RequestMapping旧版的写法]代码块，将进行替换");
            LexicalPreservingPrinter.setup(compilationUnit);
            compilationUnit.accept(visitor, null);
            visitor.getMetaDataList().forEach(meta -> addImports(compilationUnit, meta));
            deleteImports(compilationUnit);
            String newBody = LexicalPreservingPrinter.print(compilationUnit);
            // 以utf-8编码的方式写入文件中
            FileCopyUtils.copy(newBody.toString().getBytes("utf-8"), file);
        }
    }

    @Getter
    class RequestMappingConstructorVisitor extends ModifierVisitor<Void> {
        private boolean modify = false;
        private Set<Metadata> metaDataList = new HashSet<>();

        @Override
        public Visitable visit(MethodDeclaration method, Void arg) {
            NodeList<AnnotationExpr> anns = method.getAnnotations();
            for (AnnotationExpr expr : anns) {
                if ("RequestMapping".equals(expr.getNameAsString())) {
                    // 只有有注解有元素的才加入带筛查列表中
                    if (expr instanceof NormalAnnotationExpr) {
                        doHandle((NormalAnnotationExpr) expr);
                        return method;
                    }
                }
            }
            return super.visit(method, arg);
        }

        private void doHandle(NormalAnnotationExpr expr) {
            NodeList<MemberValuePair> pairs = expr.getPairs();
            MemberValuePair temp = null;
            for (MemberValuePair p : pairs) {
                if ("method".equals(p.getNameAsString())) {
                    Metadata metadata = mapping.get(p.getValue().toString());
                    String newAnnoName = metadata.getAnnName();
                    metaDataList.add(metadata);
                    // 更新注解名
                    expr.setName(newAnnoName);
                    temp = p;
                    continue;
                }
            }
            // 删除设置的method参数
            if (temp != null) {
                pairs.remove(temp);
                // 设置标志位
                modify = true;
            }
        }

    }

    private void deleteImports(CompilationUnit compilationUnit) {
        NodeList<ImportDeclaration> imports = compilationUnit.getImports();
        List<String> deleteImports = Arrays.asList(
                "org.springframework.web.bind.annotation.RequestMethod");
        imports.stream()
                .filter(it -> {
                    return deleteImports.contains(it.getName().asString());
                })
                // 不可边循环边删除,所以先filter出一个集合再删除
                .collect(Collectors.toList())
                .forEach(it -> compilationUnit.remove(it));
    }

}
