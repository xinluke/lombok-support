package com.wangym.lombok.job.impl;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.wangym.lombok.job.JavaJob;
import com.wangym.lombok.job.Metadata;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${mergeRequestUrl:false}")
    private boolean mergeRequestUrl;
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
        RequestMappingConstructorVisitor visitor = new RequestMappingConstructorVisitor(mergeRequestUrl);
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
        private List<String> annNames = Arrays.asList("RequestMapping", "GetMapping", "PostMapping", "PutMapping",
                "DeleteMapping", "PatchMapping");
        private boolean modify = false;
        private Set<Metadata> metaDataList = new HashSet<>();
        private boolean enableMergeRequestUrl;
        // 父级路径
        private String path;

        public RequestMappingConstructorVisitor(boolean enableMergeRequestUrl) {
            super();
            this.enableMergeRequestUrl = enableMergeRequestUrl;
        }

        @Override
        public Visitable visit(MethodDeclaration method, Void arg) {
            NormalAnnotationExpr expr = getTargetAnn(method);
            if (expr != null) {
                doHandle(expr);
            }
            return super.visit(method, arg);
        }

        @Override
        public Visitable visit(ClassOrInterfaceDeclaration n, Void arg) {
            NormalAnnotationExpr expr = getTargetAnn(n);
            if (expr != null) {
                NodeList<MemberValuePair> pairs = expr.getPairs();
                for (MemberValuePair p : pairs) {
                    String nameAsString = p.getNameAsString();
                    if ("value".equals(nameAsString)) {
                        // record
                        StringLiteralExpr v = (StringLiteralExpr) p.getValue();
                        recordPath(v.getValue());
                    }
                }
                if (hasPath()) {
                    n.remove(expr);
                }
            }
            return super.visit(n, arg);
        }

        void recordPath(String newPath) {
            // 只更新一次
            if (path == null && enableMergeRequestUrl) {
                path = newPath;
            }
        }

        boolean hasPath() {
            return path != null;
        }

        private NormalAnnotationExpr getTargetAnn(MethodDeclaration it) {
            NodeList<AnnotationExpr> anns = it.getAnnotations();
            for (AnnotationExpr item : anns) {
                if (annNames.contains(item.getNameAsString())) {
                    if (item instanceof NormalAnnotationExpr) {
                        return (NormalAnnotationExpr) item;
                    }
                }
            }
            return null;
        }

        private NormalAnnotationExpr getTargetAnn(ClassOrInterfaceDeclaration it) {
            NodeList<AnnotationExpr> anns = it.getAnnotations();
            for (AnnotationExpr item : anns) {
                if (annNames.contains(item.getNameAsString())) {
                    if (item instanceof NormalAnnotationExpr) {
                        return (NormalAnnotationExpr) item;
                    }
                }
            }
            return null;
        }

        private void doHandle(NormalAnnotationExpr expr) {
            NodeList<MemberValuePair> pairs = expr.getPairs();
            MemberValuePair temp = null;
            for (MemberValuePair p : pairs) {
                String nameAsString = p.getNameAsString();
                if ("method".equals(nameAsString)) {
                    Expression value = p.getValue();
                    Metadata metadata;
                    // 判断是否是数组类型的注解值
                    if (value instanceof ArrayInitializerExpr) {
                        ArrayInitializerExpr val = (ArrayInitializerExpr) value;
                        NodeList<Expression> annParamValuesList = val.getValues();
                        // 如果是多个也无法确定是替换成哪种形式
                        if (annParamValuesList.size() != 1) {
                            continue;
                        } else {
                            // 默认取第0个
                            metadata = mapping.get(annParamValuesList.get(0).toString());
                        }
                    } else {
                        metadata = mapping.get(value.toString());
                    }
                    String newAnnoName = metadata.getAnnName();
                    metaDataList.add(metadata);
                    // 更新注解名
                    expr.setName(newAnnoName);
                    temp = p;
                } else if ("path".equals(nameAsString)) {
                    // 使用path的参数全部切换成value的方式，统一
                    p.setName(new SimpleName("value"));
                    modify = true;
                } else if ("value".equals(nameAsString)) {
                    Expression value = p.getValue();
                    // 判断是否是数组类型的注解值
                    if (value instanceof ArrayInitializerExpr) {
                        ArrayInitializerExpr val = (ArrayInitializerExpr) value;
                        NodeList<Expression> annParamValuesList = val.getValues();
                        // 如果是只有一个参数就用简单写法，简约&规范
                        if (annParamValuesList.size() == 1) {
                            p.setValue(annParamValuesList.get(0));
                            modify = true;
                        }
                    } else if (value instanceof StringLiteralExpr) {
                        if (hasPath()) {
                            StringLiteralExpr v = (StringLiteralExpr) value;
                            p.setValue(new StringLiteralExpr(path + v.getValue()));
                            modify = true;
                        }
                    }
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
