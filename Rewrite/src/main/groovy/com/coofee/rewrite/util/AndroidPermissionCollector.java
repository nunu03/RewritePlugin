package com.coofee.rewrite.util;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class AndroidPermissionCollector {

    public static class MethodPermissionInfo {
        public final String fullClassName;
        public final String methodName;
        public final LinkedHashSet<String> permissions;

        public MethodPermissionInfo(String fullClassName, String methodName, String... permissions) {
            this.fullClassName = fullClassName;
            this.methodName = methodName;
            this.permissions = new LinkedHashSet<>(Arrays.asList(permissions));
        }

        public MethodPermissionInfo(String fullClassName, String methodName, Collection<String> permissions) {
            this.fullClassName = fullClassName;
            this.methodName = methodName;
            this.permissions = new LinkedHashSet<>(permissions);
        }
    }


    public static class ClassMethodPermissionInfo {
        public final String fullClassName;
        public final Map<String, MethodPermissionInfo> methodMap = new LinkedHashMap<>();

        public ClassMethodPermissionInfo(String fullClassName) {
            this.fullClassName = fullClassName;
        }

        public void add(String fullClassName, String methodName, String... permissions) {
            if (permissions == null) {
                return;
            }

            add(fullClassName, methodName, Arrays.asList(permissions));
        }

        public void add(String fullClassName, String methodName, Collection<String> permissions) {
            if (permissions == null || permissions.isEmpty()) {
                return;
            }

            MethodPermissionInfo methodPermissionInfo = methodMap.get(methodName);
            if (methodPermissionInfo == null) {
                methodPermissionInfo = new MethodPermissionInfo(fullClassName, methodName, permissions);
                methodMap.put(methodName, methodPermissionInfo);
            } else {
                methodPermissionInfo.permissions.addAll(permissions);
            }
        }

    }

    public static class AndroidFrameworkMethodPermissionInfo {
        public Map<String, ClassMethodPermissionInfo> classPermissionInfoMap = new HashMap<>();

        public void add(String fullClassName, String methodName, String... permissions) {
            if (permissions == null) {
                return;
            }

            add(fullClassName, methodName, Arrays.asList(permissions));
        }

        public void add(String fullClassName, String methodName, Collection<String> permissions) {
            if (permissions == null || permissions.isEmpty()) {
                return;
            }

            System.out.println("fullClassName=" + fullClassName + ", methodName=" + methodName + ", permissions=" + permissions);
            ClassMethodPermissionInfo cLassMethodPermissionInfo = classPermissionInfoMap.get(fullClassName);
            if (cLassMethodPermissionInfo == null) {
                cLassMethodPermissionInfo = new ClassMethodPermissionInfo(fullClassName);
                classPermissionInfoMap.put(fullClassName, cLassMethodPermissionInfo);
            }

            cLassMethodPermissionInfo.add(fullClassName, methodName, permissions);
        }

    }

    public static class RequiresPermissionVisitorAdapter extends VoidVisitorAdapter<Object> {
        private final Set<String> nameSet = new LinkedHashSet<>(Arrays.asList("RequiresPermission"));

        public final AndroidFrameworkMethodPermissionInfo methodPermissionInfo = new AndroidFrameworkMethodPermissionInfo();

        @Override
        public void visit(MethodDeclaration methodDeclaration, Object arg) {
            NodeList<AnnotationExpr> annotations = methodDeclaration.getAnnotations();
            if (annotations == null || annotations.isEmpty()) {
                return;
            }

            Iterator<AnnotationExpr> iterator = annotations.iterator();
            while (iterator.hasNext()) {
                AnnotationExpr next = iterator.next();
                String annotationClassName = next.getNameAsString();
                if (!nameSet.contains(annotationClassName)) {
                    continue;
                }

//                try {
//                    qualifiedName = next.resolve().getQualifiedName();
//                } catch (Throwable e) {
//                    qualifiedName = next.getNameAsString();
//                    e.printStackTrace();
//                }

                final String fullClassName = ((ClassOrInterfaceDeclaration) methodDeclaration.getParentNode().get())
                        .getFullyQualifiedName().get();
                final String methodName = methodDeclaration.getNameAsString();

                if (next instanceof NormalAnnotationExpr) {
                    Collection<String> permissions = handleNormalAnnotationExpr((NormalAnnotationExpr) next);
                    methodPermissionInfo.add(fullClassName, methodName, permissions);

                } else if (next instanceof SingleMemberAnnotationExpr) {
                    String permission = handleSingleMemberAnnotationExpr((SingleMemberAnnotationExpr) next);
                    methodPermissionInfo.add(fullClassName, methodName, permission);
                }
            }
        }

        private Collection<String> handleNormalAnnotationExpr(NormalAnnotationExpr annotationExpr) {
            NodeList<MemberValuePair> pairs = annotationExpr.getPairs();
            if (pairs == null) {
                return null;
            }


            final Set<String> permissions = new HashSet<>();
            Iterator<MemberValuePair> iterator = pairs.iterator();
            while (iterator.hasNext()) {
                MemberValuePair memberValuePair = iterator.next();
                String nameAsString = memberValuePair.getNameAsString();
//                                                allOf.asArrayInitializerExpr().getValues().get(0)
                if ("value".equals(nameAsString)) {
                    Expression value = memberValuePair.getValue();
                    permissions.add(convertToFullPermission(value.toString()));

                } else if ("allOf".equals(nameAsString) || "anyOf".equals(nameAsString)) {
                    NodeList<Expression> values = memberValuePair.getValue().asArrayInitializerExpr().getValues();
                    values.forEach(value -> permissions.add(convertToFullPermission(value.toString())));
                }
            }

            return permissions;
        }

        private String handleSingleMemberAnnotationExpr(SingleMemberAnnotationExpr annotationExpr) {
            return convertToFullPermission(annotationExpr.getMemberValue().toString());
        }

        private String convertToFullPermission(String permission) {
            permission = permission.replace("\"", "");
            int i = permission.lastIndexOf('.');
            String fullPermission = permission;
            if (i == -1) {
                fullPermission = "android.Manifest.permission." + permission;
                System.out.println("convert permission=" + permission + " to " + fullPermission);
                return fullPermission;
            }

            if (permission.startsWith("android.Manifest.permission.")) {
                return permission;
            }

            fullPermission = "android.Manifest.permission." + permission.substring(i + 1);
            System.out.println("convert permission=" + permission + " to " + fullPermission);
            return fullPermission;
        }
    }

    public static class ClassItem {
        public static class MethodItem {
            public final String methodName;
            public final Set<String> permissions;

            public MethodItem(String methodName, Set<String> permissions) {
                this.methodName = methodName;
                this.permissions = permissions;
            }
        }

        public final String className;
        public final List<MethodItem> methods = new ArrayList<>();

        public ClassItem(String className) {
            this.className = className;
        }

        public void add(String methodName, Set<String> permissions) {
            this.methods.add(new MethodItem(methodName, permissions));
        }

        public static Map<String, Collection<String>> convertToClassMethodPermissions(Collection<ClassItem> classItems) {
            Map<String, Collection<String>> classMethodPermissionMap = new LinkedHashMap<>();
            for (ClassItem classItem : classItems) {
                for (MethodItem methodItem : classItem.methods) {
                    String key = classItem.className + "#" + methodItem.methodName;
                    classMethodPermissionMap.put(key, methodItem.permissions);
                }
            }
            return classMethodPermissionMap;
        }
    }

    public static void collect(File androidSourceCodeDir, File outClassListJsonFile, File outMethodPermissionJsonFile) {
        if (!androidSourceCodeDir.exists()) {
            System.out.println("[RewritePlugin] android source code dir doest exists; " + androidSourceCodeDir);
            return;
        }

        ParserConfiguration config = new ParserConfiguration();
        config.setSymbolResolver(new JavaSymbolSolver(new ReflectionTypeSolver()));
        StaticJavaParser.setConfiguration(config);

        RequiresPermissionVisitorAdapter requiresPermissionVisitorAdapter = new RequiresPermissionVisitorAdapter();
        accept(androidSourceCodeDir, file -> {
            if (file.getName().endsWith(".java")) {

                try {
//                    System.out.println("[RewritePlugin] parse file=" + file);
                    CompilationUnit compilationUnit = StaticJavaParser.parse(file);
                    compilationUnit.accept(requiresPermissionVisitorAdapter, null);

                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });

        Collection<ClassMethodPermissionInfo> classes = requiresPermissionVisitorAdapter.methodPermissionInfo.classPermissionInfoMap.values();
        List<ClassItem> resultList = new ArrayList<>(classes.size());

        for (ClassMethodPermissionInfo classInfo : classes) {
            ClassItem classItem = new ClassItem(classInfo.fullClassName);

            Collection<MethodPermissionInfo> methods = classInfo.methodMap.values();
            for (MethodPermissionInfo methodInfo : methods) {
                classItem.add(methodInfo.methodName, methodInfo.permissions);
            }

            resultList.add(classItem);
        }

        try {
            String json = new Gson().toJson(resultList);
//            File file = new File("android_framework_method_permission_by_class.json");
            org.apache.commons.io.FileUtils.writeStringToFile(outClassListJsonFile, json);
            System.out.println("[RewritePlugin] success write to file=" + outClassListJsonFile);
        } catch (IOException e) {
            new Throwable("[RewritePlugin] fail write to file=" + outClassListJsonFile, e).printStackTrace();
        }

        Map<String, Collection<String>> classMethodPermissions = ClassItem.convertToClassMethodPermissions(resultList);
        try {
            String json = new Gson().toJson(classMethodPermissions);
//            File file = new File("android_framework_class_method_permission.json");
            org.apache.commons.io.FileUtils.writeStringToFile(outMethodPermissionJsonFile, json);
            System.out.println("[RewritePlugin] success write to file=" + outMethodPermissionJsonFile);
        } catch (IOException e) {
            new Throwable("[RewritePlugin] fail write to file=" + outMethodPermissionJsonFile, e).printStackTrace();
        }
    }

    public static void main(String[] args) {
        String home = System.getProperty("user.home");
        System.out.println("home=" + home);
        File androidSourceCodeDir = new File(home + "/ide/adt-bundle-mac-x86_64-20140624/sdk/sources/android-29");
        File outClassListJsonFile = new File("android_framework_method_permission_by_class.json");
        File outMethodPermissionJsonFile = new File("android_framework_class_method_permission.json");
        collect(androidSourceCodeDir, outClassListJsonFile, outMethodPermissionJsonFile);
    }

    public static void accept(File rootDir, Consumer<File> fileConsumer) {
        if (rootDir == null || !rootDir.exists()) {
            return;
        }

        if (rootDir.isFile()) {
            fileConsumer.accept(rootDir);
            return;
        }

        File[] files = rootDir.listFiles();
        if (files == null) {
            return;
        }

        for (File child : files) {
            if (child.isFile()) {
                fileConsumer.accept(child);
            } else {
                accept(child, fileConsumer);
            }
        }
    }
}
