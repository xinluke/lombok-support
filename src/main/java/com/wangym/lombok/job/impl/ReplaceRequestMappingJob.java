package com.wangym.lombok.job.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.Visitable;
import com.wangym.lombok.job.AbstractJavaJob;
import com.wangym.lombok.job.Metadata;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author wangym
 * @version 创建时间：2018年6月7日 上午9:42:59
 */
@Component
@Slf4j
@Setter
public class ReplaceRequestMappingJob extends AbstractJavaJob {
    private List<String> annNames = Arrays.asList("RequestMapping", "GetMapping", "PostMapping", "PutMapping",
            "DeleteMapping", "PatchMapping");
    // 是否要将类上面的url和方法拼接到一起
    @Value("${mergeRequestUrl:false}")
    private boolean enableMergeRequestUrl;
    @Value("${onlySupportJson:false}")
    private boolean onlySupportJson;
    @Value("${addApiOperation:false}")
    private boolean apiOperation;
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
    public void process(CompilationUnit compilationUnit) {
        int before = compilationUnit.hashCode();
        RequestMappingConstructorVisitor visitor = new RequestMappingConstructorVisitor(compilationUnit);
        compilationUnit.accept(visitor, null);
        compilationUnit.accept(new RequiresPermissionsVisitor(), null);
        if (apiOperation) {
            compilationUnit.accept(new ApiOperationVisitor(), null);
        }
        // 如果存在变更，则操作
        if (before != compilationUnit.hashCode()) {
            deleteImports(compilationUnit);
        }
    }

    @Override
    public void applyPreProcess(CompilationUnit compilationUnit, String path) {
        compilationUnit.accept(new RequestMappingMonitorVisitor(path), null);
    }

    class RequestMappingMonitorVisitor extends AbstractRequestMappingVisitor {
        private String path;

        public RequestMappingMonitorVisitor(String path) {
            super();
            this.path = path;
        }

        @Override
        public Visitable visit(MethodDeclaration method, Void arg) {
            // 找出方法中的@RequestMapping，并打印出来,需要整改。推荐做法：声明接口写明http method
            NormalAnnotationExpr expr = getTargetAnn(method, annNames);
            if (expr != null) {
                log(expr);
            }
            SingleMemberAnnotationExpr singleExpr = getSingleTargetAnn(method, annNames);
            if (singleExpr != null) {
                log(singleExpr);
            }
            return super.visit(method, arg);
        }

        private void log(AnnotationExpr ann) {
            if (ann.toString().startsWith("@RequestMapping")) {
//                PackageDeclaration pkg = ann.findCompilationUnit().get().getPackageDeclaration().get();
//                ClassOrInterfaceDeclaration parent = ann.findAncestor(ClassOrInterfaceDeclaration.class).get();
//                String qualified = pkg.getNameAsString() + "." + parent.getNameAsString();
                log.warn("find @RequestMapping method:[{}],path:[{}]", ann, path);
            }
        }
    }

    class RequiresPermissionsVisitor extends AbstractRequestMappingVisitor {
        private NormalAnnotationExpr expr;

        @Override
        public Visitable visit(ClassOrInterfaceDeclaration n, Void arg) {
            // 查看类上面是否有此注解
            expr = getTargetAnn(n, Arrays.asList("RequiresPermissions"));
            if (expr != null) {
                n.remove(expr);
            }
            return super.visit(n, arg);
        }

        @Override
        public Visitable visit(MethodDeclaration method, Void arg) {
            // 确定是否是RequestMappingEndpoint
            if (getTargetAnn(method, annNames) == null) {
                return super.visit(method, arg);
            }
            // 如果方法上面没有自定义的注解，则准备继承来自父类的接口
            NormalAnnotationExpr methodExpr = getTargetAnn(method, Arrays.asList("RequiresPermissions"));
            if (methodExpr == null && expr != null) {
                method.getAnnotations().add(expr);
            }
            return super.visit(method, arg);
        }
    }

    class ApiOperationVisitor extends AbstractRequestMappingVisitor {
        @Override
        public Visitable visit(MethodDeclaration method, Void arg) {
            // 确定是否是RequestMappingEndpoint
            if (getTargetAnn(method, annNames) == null) {
                return super.visit(method, arg);
            }
            // 如果方法上面没有自定义的注解，则准备添加一个
            SingleMemberAnnotationExpr singleExpr = getSingleTargetAnn(method, Arrays.asList("ApiOperation"));
            if (singleExpr != null) {
                method.remove(singleExpr);
                // 原本注解的内容移过去
                StringLiteralExpr expr = (StringLiteralExpr) singleExpr.getMemberValue();
                String val = expr.asString();
                // 在第一个位置进行插入
                method.getAnnotations().addLast(geneApiOperationAnn(val));
            }
            NormalAnnotationExpr methodExpr = getTargetAnn(method, Arrays.asList("ApiOperation"));
            if (methodExpr == null) {
                // 在第一个位置进行插入
                method.getAnnotations().addFirst(geneApiOperationAnn(""));
            }
            return super.visit(method, arg);
        }

        private NormalAnnotationExpr geneApiOperationAnn(String val) {
            // 构造一个标准的注解格式
            MemberValuePair p = new MemberValuePair("value", new StringLiteralExpr(val));
            NormalAnnotationExpr ann = new NormalAnnotationExpr(new Name("ApiOperation"), new NodeList<>(p));
            return ann;
        }
    }

    class RequestMappingConstructorVisitor extends AbstractRequestMappingVisitor {
        private List<String> annNames = Arrays.asList("RequestMapping", "GetMapping", "PostMapping", "PutMapping",
                "DeleteMapping", "PatchMapping");
        // 父级路径
        private String path;
        private CompilationUnit compilationUnit;
        private boolean isInterface = false;

        public RequestMappingConstructorVisitor(CompilationUnit compilationUnit) {
            super();
            this.compilationUnit = compilationUnit;
        }

        @Override
        public Visitable visit(MethodDeclaration method, Void arg) {
            NormalAnnotationExpr expr = getTargetAnn(method, annNames);
            if (expr != null) {
                doHandle(expr);
            }
            SingleMemberAnnotationExpr singleExpr = getSingleTargetAnn(method, annNames);
            if (singleExpr != null) {
                doHandle(singleExpr);
            }
            return super.visit(method, arg);
        }

        @Override
        public Visitable visit(ClassOrInterfaceDeclaration n, Void arg) {
            // 从类上拿到注解
            isInterface = n.isInterface();
            searchPath(n);
            searchPath0(n);
            return super.visit(n, arg);
        }
        private void searchPath(ClassOrInterfaceDeclaration n) {
            NormalAnnotationExpr expr = getTargetAnn(n, annNames);
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
        }

        private void searchPath0(ClassOrInterfaceDeclaration n) {
            SingleMemberAnnotationExpr singleExpr = getSingleTargetAnn(n, annNames);
            if (singleExpr != null) {
                StringLiteralExpr expr = (StringLiteralExpr) singleExpr.getMemberValue();
                // 这里面的值必然是url
                String val = expr.asString();
                // record
                recordPath(val);
                if (hasPath()) {
                    n.remove(singleExpr);
                }
            }
        }

        void recordPath(String newPath) {
            // 只更新一次
            if (path == null && enableMergeRequestUrl && isInterface) {
                path = newPath;
            }
        }

        boolean hasPath() {
            return path != null;
        }

        private void doHandle(SingleMemberAnnotationExpr expr) {
            StringLiteralExpr v = (StringLiteralExpr) expr.getMemberValue();
            if (hasPath()) {
                // 合并url
                // v.setString(path + v.getValue());
                // 上述写法好像不会生效，换下述写法
                expr.setMemberValue(new StringLiteralExpr(path + v.getValue()));
            }
        }
        private void doHandle(NormalAnnotationExpr expr) {
            NodeList<MemberValuePair> pairs = expr.getPairs();
            MemberValuePair temp = null;
            boolean producesExist = false;
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
                    addImports(compilationUnit, metadata);
                    String newAnnoName = metadata.getAnnName();
                    // 更新注解名
                    expr.setName(newAnnoName);
                    temp = p;
                } else if ("path".equals(nameAsString)) {
                    // 使用path的参数全部切换成value的方式，统一
                    p.setName(new SimpleName("value"));
                } else if ("value".equals(nameAsString)) {
                    Expression value = p.getValue();
                    // 判断是否是数组类型的注解值
                    if (value instanceof ArrayInitializerExpr) {
                        ArrayInitializerExpr val = (ArrayInitializerExpr) value;
                        NodeList<Expression> annParamValuesList = val.getValues();
                        // 如果是只有一个参数就用简单写法，简约&规范
                        if (annParamValuesList.size() == 1) {
                            p.setValue(annParamValuesList.get(0));
                        }
                    } else if (value instanceof StringLiteralExpr) {
                        if (hasPath()) {
                            StringLiteralExpr v = (StringLiteralExpr) value;
                            p.setValue(new StringLiteralExpr(path + v.getValue()));
                        }
                    }
                } else if ("produces".equals(nameAsString)) {
                    // 如果当前写法已经指明了produces，建议不再变动它
                    producesExist = true;
                }
            }
            if(onlySupportJson && !producesExist) {
                // 有些开发者希望api返回的格式是固定的，所以直接在注解声明只支持json格式的数据响应
                // 避免有些开发者不清楚自己使用了Http的Accept协商头，而我们的api假如是多种返回格式的话，可能会返回xml或者其他格式的数据，而重点在于开发者不太了解http的协商机制，从而认为是我们的问题。
                // 现阶段使用json格式的数据应该是满足绝大部分的用户的需求
                FieldAccessExpr value = new FieldAccessExpr(new NameExpr("MediaType"), "APPLICATION_JSON_VALUE");
                pairs.add(new MemberValuePair("produces", value));
            }
            // 删除设置的method参数
            if (temp != null) {
                pairs.remove(temp);
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
