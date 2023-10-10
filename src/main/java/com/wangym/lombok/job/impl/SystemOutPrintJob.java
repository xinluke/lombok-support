package com.wangym.lombok.job.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.wangym.lombok.job.AbstractJavaJob;
import com.wangym.lombok.job.Metadata;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wangym
 * @version 创建时间：2018年11月6日 下午2:27:46
 */
@Component
@Slf4j
public class SystemOutPrintJob extends AbstractJavaJob {
    public static final Name FEIGN_CLIENT = new Name("FeignClient");
    private Metadata meta = new Metadata("Slf4j", "lombok.extern.slf4j.Slf4j");
    private Metadata meta2 = new Metadata("Autowired", "org.springframework.beans.factory.annotation.Autowired");
    private Metadata meta3 = new Metadata("Synchronized", "lombok.Synchronized");
    private Metadata metaHashMap = new Metadata("HashMap", "java.util.HashMap");
    private Metadata metaArrayList = new Metadata("ArrayList", "java.util.ArrayList");
    @Value("${synchronizedAnnotationSupport:false}")
    private boolean synchronizedAnnotationSupport;

    @Override
    public void process(CompilationUnit compilationUnit) {
        compilationUnit.accept(new SynchronizedMehtodVisitor(compilationUnit), null);
        SystemOutPrintVisitor visitor = new SystemOutPrintVisitor(compilationUnit);
        compilationUnit.accept(new MainMehtodVisitor(), null);
        compilationUnit.accept(new GuavaVisitor(compilationUnit), null);
        compilationUnit.accept(visitor, null);
        compilationUnit.accept(new ValueVisitor(), null);
        compilationUnit.accept(new CommentVistor(), null);
        compilationUnit.accept(new FeignClientVisitor(), null);
        AutowiredVisitor visit = new AutowiredVisitor();
        compilationUnit.accept(visit, null);
        if (visit.isFlag()) {
            addImports(compilationUnit, meta2);
        }
    }

    @Override
    public void applyPreProcess(CompilationUnit compilationUnit, String path) {
        compilationUnit.accept(new FeignClientCheckVisitor(path), null);
        compilationUnit.accept(new ModifierVisitor<Void>() {
            @Override
            public Visitable visit(MethodDeclaration n, Void arg) {
                NodeList<Modifier> set = n.getModifiers();
                String methodName = n.getNameAsString();
                boolean flag = set.contains(Modifier.staticModifier()) && set.contains(Modifier.publicModifier());
                if (flag && "main".equals(methodName)) {
                    ClassOrInterfaceDeclaration parent = n.findAncestor(ClassOrInterfaceDeclaration.class).get();
                    // 找出只集成spring boot的运行主类
                    if (hasAnnotation(parent, "SpringBootApplication") && !hasAnnotation(parent, "EnableEurekaClient")
                            && !hasAnnotation(parent, "EnableDiscoveryClient")) {
                        log.warn("find only spring boot project:{}", path);
                    }
                }
                return super.visit(n, arg);
            }

            private boolean hasAnnotation(ClassOrInterfaceDeclaration parent, String annotationName) {
                NodeList<AnnotationExpr> anns = parent.getAnnotations();
                boolean exist = anns.stream()
                        .filter(it -> annotationName.equals(it.getNameAsString()))
                        .count() > 0;
                return exist;
            }
        }, null);
    }

    ;

    class CommentVistor extends ModifierVisitor<Void> {
        @Override
        public Visitable visit(Modifier n, Void arg) {
            return super.visit(n, arg);
        }

        @Override
        public Visitable visit(MethodDeclaration n, Void arg) {
            //将非javadoc注释换成javadoc注释
            if (n.getComment().isPresent()) {
                Comment comment = n.getComment().get();
                //只处理单行注释
                if (comment instanceof LineComment) {
                    String content = comment.getContent();
                    n.setJavadocComment(content);
                }
            }
            return super.visit(n, arg);
        }
    }

    class ValueVisitor extends ModifierVisitor<Void> {
        @Override
        public Visitable visit(FieldDeclaration n, Void arg) {
            Expression targetValExpr = null;
            NodeList<AnnotationExpr> anns = n.getAnnotations();
            // 将字段上面的@Value(value="${black.list}")去掉value=，统一格式
            for (Iterator<AnnotationExpr> iterator = anns.iterator(); iterator.hasNext(); ) {
                AnnotationExpr annotationExpr = iterator.next();
                if ("Value".equals(annotationExpr.getNameAsString())) {
                    if (annotationExpr instanceof NormalAnnotationExpr) {
                        NormalAnnotationExpr naExpr = (NormalAnnotationExpr) annotationExpr;
                        SimpleName simpleName = new SimpleName("value");
                        List<MemberValuePair> list = naExpr.getPairs()
                                .stream()
                                .filter(it -> {
                                    return it.getName().equals(simpleName);
                                })
                                .collect(Collectors.toList());
                        if (list.size() == 1) {
                            targetValExpr = list.get(0).getValue();
                            iterator.remove();
                        }
                    }
                }
            }
            if (targetValExpr != null) {
                n.addAnnotation(new SingleMemberAnnotationExpr(new Name("Value"), targetValExpr));
            }
            return super.visit(n, arg);
        }
    }

    @Getter
    class AutowiredVisitor extends ModifierVisitor<Void> {
        // 是否存在任一个修改
        private boolean flag;

        @Override
        public Visitable visit(FieldDeclaration n, Void arg) {
            boolean record = false;
            NodeList<AnnotationExpr> anns = n.getAnnotations();
            // 将字段上面的@Resource换成@Autowired,统一管理
            for (Iterator<AnnotationExpr> iterator = anns.iterator(); iterator.hasNext(); ) {
                AnnotationExpr annotationExpr = iterator.next();
                if ("Resource".equals(annotationExpr.getNameAsString())) {
                    iterator.remove();
                    flag = true;
                    record = true;
                }
            }
            if (record) {
                n.addAnnotation(new MarkerAnnotationExpr("Autowired"));
                // anns.add();
            }
            return super.visit(n, arg);
        }
    }

    class FeignClientVisitor extends ModifierVisitor<Void> {

        @Override
        public Visitable visit(SingleMemberAnnotationExpr n, Void arg) {
            //FeignClient注解写法标准化
            if (n.getName().equals(FEIGN_CLIENT)) {
                NodeList<MemberValuePair> pairs = new NodeList<>();
                //字段名叫name比叫value好，简明扼要，表达清晰
                pairs.add(new MemberValuePair("name", n.getMemberValue()));
                return new NormalAnnotationExpr(FEIGN_CLIENT, pairs);
            }
            return super.visit(n, arg);
        }


    }

    class FeignClientCheckVisitor extends ModifierVisitor<Void> {

        private String path;

        public FeignClientCheckVisitor(String path) {
            super();
            this.path = path;
        }

        @Override
        public Visitable visit(ClassOrInterfaceDeclaration n, Void arg) {
            // 找出@FeignClient
            boolean flag = false;
            NodeList<AnnotationExpr> anns = n.getAnnotations();
            for (AnnotationExpr annotationExpr : anns) {
                if (annotationExpr.toString().startsWith("@FeignClient")) {
                    if (annotationExpr instanceof NormalAnnotationExpr) {
                        NormalAnnotationExpr a = (NormalAnnotationExpr) annotationExpr;
                        long count = a.getPairs().stream().filter(it -> it.getName().toString().equals("url")).count();
                        if (count == 0) {
                            flag = true;
                            break;
                        }
                    } else {
                        flag = true;
                        break;
                    }
                }
            }
            boolean hasMethod = !n.getMethods().isEmpty();
            // 判断是否有继承关系，是不是单体
            boolean standalone = n.getExtendedTypes().isEmpty();
            if (flag && n.isInterface() && (standalone || hasMethod)) {
                // String name = getQualifiedName(n);
                log.info("find target FeignClient:{},path:{}", n.getNameAsString(), path);
            }
            return super.visit(n, arg);
        }

        private String getQualifiedName(ClassOrInterfaceDeclaration n) {
            PackageDeclaration pkg = n.findCompilationUnit().get().getPackageDeclaration().get();
            String qualified = pkg.getNameAsString() + "." + n.getNameAsString();
            return qualified;
        }
    }

    class GuavaVisitor extends ModifierVisitor<Void> {
        private Map<MethodCallExpr, ExprWrapper> map = new HashMap<>();
        private CompilationUnit compilationUnit;

        public GuavaVisitor(CompilationUnit compilationUnit) {
            super();
            this.compilationUnit = compilationUnit;
            // 替换guava提供的泛型推导创建集合的方式，1.8中java本身已经可以直接推导
            map.put(new MethodCallExpr(new NameExpr("Lists"), "newArrayList"),
                    new ExprWrapper(
                            new ObjectCreationExpr(null, new ClassOrInterfaceType("ArrayList<>"), new NodeList<>()),
                            metaArrayList));
            map.put(new MethodCallExpr(new NameExpr("Maps"), "newHashMap"),
                    new ExprWrapper(
                            new ObjectCreationExpr(null, new ClassOrInterfaceType("HashMap<>"), new NodeList<>()),
                            metaHashMap));
        }

        @Override
        public Visitable visit(MethodCallExpr n, Void arg) {
            for (Map.Entry<MethodCallExpr, ExprWrapper> entry : map.entrySet()) {
                if (n.equals(entry.getKey())) {
                    ExprWrapper value = entry.getValue();
                    addImports(compilationUnit, value.getMeta());
                    return value.getExpr();
                }
            }
            return super.visit(n, arg);
        }

        @Getter
        class ExprWrapper {
            private ObjectCreationExpr expr;
            private Metadata meta;

            public ExprWrapper(ObjectCreationExpr expr, Metadata meta) {
                super();
                this.expr = expr;
                this.meta = meta;
            }

        }
    }

    class SystemOutPrintVisitor extends ModifierVisitor<Void> {

        private CompilationUnit compilationUnit;

        public SystemOutPrintVisitor(CompilationUnit compilationUnit) {
            super();
            this.compilationUnit = compilationUnit;
        }

        @Override
        public Visitable visit(MethodCallExpr n, Void arg) {
            Optional<Expression> scope = n.getScope();
            // 找出System.out.println();和System.out.print();
            if (scope.isPresent() && "System.out".equals(scope.get().toString())
                    && n.getNameAsString().startsWith("print")) {
                log.info("存在[System.out.println()]代码块，将进行替换");
                ClassOrInterfaceDeclaration parent = n.findAncestor(ClassOrInterfaceDeclaration.class).get();
                addAnnotation(parent, meta);
                addImports(compilationUnit, meta);
                return process(n);
            }
            return super.visit(n, arg);
        }

        private MethodCallExpr process(MethodCallExpr expr) {
            NodeList<Expression> args = expr.getArguments();
            int size = args.size();
            if (size == 0) {
                // System.out.println();是没有意义的，直接删除掉
                return null;
            }
            if (size == 1) {
                Expression arg = args.get(0);
                // 如果判断参数类型不是String的话就应该包装print
                if (!(arg instanceof StringLiteralExpr || arg instanceof BinaryExpr)) {
                    args.add(0, new StringLiteralExpr("print:{}"));
                }
            }
            String str = "log";
            expr.setScope(new NameExpr(str));
            expr.setName("info");
            return expr;
        }
    }

    class SynchronizedMehtodVisitor extends ModifierVisitor<Void> {
        private CompilationUnit compilationUnit;

        public SynchronizedMehtodVisitor(CompilationUnit compilationUnit) {
            super();
            this.compilationUnit = compilationUnit;
        }

        @Override
        public Visitable visit(MethodDeclaration n, Void arg) {
            //如果存在Synchronized的方法块，需要转成lombok的注解
            if (synchronizedAnnotationSupport && n.isSynchronized()) {
                NodeList<Modifier> mo = n.getModifiers();
                mo.remove(Modifier.synchronizedModifier());
                n.addAnnotation(new MarkerAnnotationExpr(meta3.getAnnName()));
                addImports(compilationUnit, meta3);
            }
            return super.visit(n, arg);
        }
    }

    class MainMehtodVisitor extends ModifierVisitor<Void> {
        @Override
        public Visitable visit(MethodDeclaration n, Void arg) {
            NodeList<Modifier> set = n.getModifiers();
            String methodName = n.getNameAsString();
            boolean flag = set.contains(Modifier.staticModifier()) && set.contains(Modifier.publicModifier());
            if (flag && "main".equals(methodName)) {
                BlockStmt body = n.getBody().get();
                if (body.getChildNodes().isEmpty()) {
                    log.info("delete empty method:{}", n);
                    // 删除掉main入口方法
                    return null;
                }
            }
            return super.visit(n, arg);
        }
    }

}
