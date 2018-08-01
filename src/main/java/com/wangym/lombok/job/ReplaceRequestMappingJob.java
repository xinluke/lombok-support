package com.wangym.lombok.job;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
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
public class ReplaceRequestMappingJob implements Job {

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
        List<MethodDeclaration> list = compilationUnit.findAll(MethodDeclaration.class);
        List<NormalAnnotationExpr> annList = new ArrayList<>();
        for (MethodDeclaration method : list) {
            NodeList<AnnotationExpr> anns = method.getAnnotations();
            for (AnnotationExpr expr : anns) {
                if ("RequestMapping".equals(expr.getNameAsString())) {
                    annList.add((NormalAnnotationExpr) expr);
                }
            }
        }
        if (annList.isEmpty()) {
            return;
        }
        LexicalPreservingPrinter.setup(compilationUnit);
        // 缓存添加的注解
        Set<Metadata> cache = new HashSet<>();
        for (NormalAnnotationExpr expr : annList) {
            NodeList<MemberValuePair> pairs = expr.getPairs();
            MemberValuePair temp = null;
            for (MemberValuePair p : pairs) {
                if ("method".equals(p.getNameAsString())) {
                    Metadata metadata = mapping.get(p.getValue().toString());
                    cache.add(metadata);
                    String newAnnoName = metadata.getAnnName();
                    // 更新注解名
                    expr.setName(newAnnoName);
                    temp = p;
                    continue;
                }
            }
            // 删除设置的method参数
            if (temp != null) {
                pairs.remove(temp);
            }
        }
        addImports(compilationUnit, cache);
        String newBody = LexicalPreservingPrinter.print(compilationUnit);
        // 暂时使用正则表达式的方式修正格式错误的问题
        newBody = newBody.replaceAll(";import", ";\n\nimport");
        // 以utf-8编码的方式写入文件中
        FileCopyUtils.copy(newBody.toString().getBytes("utf-8"), file);
    }

    @Getter
    static class Metadata {
        private String annName;
        private String ImportPkg;

        public Metadata(String annName, String importPkg) {
            super();
            this.annName = annName;
            ImportPkg = importPkg;
        }

    }

    private void addImports(CompilationUnit compilationUnit, Set<Metadata> cache) {
        List<String> addImport = cache.stream().map(Metadata::getImportPkg).collect(Collectors.toList());
        NodeList<ImportDeclaration> imports = compilationUnit.getImports();
        for (ImportDeclaration it : imports) {
            String pkg = it.getName().asString();
            boolean containMode = it.isAsterisk();
            if (addImport.isEmpty()) {
                break;
            }
            for (Iterator<String> iterator = addImport.iterator(); iterator.hasNext();) {
                String string = iterator.next();
                if (containMode) {
                    if (string.startsWith(pkg)) {
                        iterator.remove();
                    }
                } else {
                    if (string.equals(pkg)) {
                        iterator.remove();
                    }
                }
            }
        }
        for (String str : addImport) {
            imports.add(new ImportDeclaration(str, false, false));
        }
    }

}
