package com.coofee.rewrite.hierarchy;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClassHierarchy {

    private final Map<String, ClassNode> classNodeMap = new ConcurrentHashMap<>();

    private static class Holder {
        public static final ClassHierarchy INSTANCE = new ClassHierarchy();
    }

    public static void add(ClassNode classNode) {
        System.out.println("add classNode=" + classNode);
        Holder.INSTANCE.classNodeMap.put(classNode.name, classNode);
    }

    public static ClassNode get(String name) {
        return Holder.INSTANCE.classNodeMap.get(name);
    }

    public static boolean contains(String name) {
        return Holder.INSTANCE.classNodeMap.containsKey(name);
    }

    public static Map<String, ClassNode> getAll() {
        return new HashMap<>(Holder.INSTANCE.classNodeMap);
    }
}
