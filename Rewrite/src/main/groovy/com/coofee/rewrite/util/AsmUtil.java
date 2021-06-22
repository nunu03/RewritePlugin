package com.coofee.rewrite.util;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public class AsmUtil {

    public static String getClassName(byte[] classBytecode) {
        ClassReader classReader = new ClassReader(classBytecode);
        return classReader.getClassName();
    }

    public static ClassNode convert(byte[] classBytecode) {
        ClassReader classReader = new ClassReader(classBytecode);
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
        return classNode;
    }

    public static byte[] convert(ClassNode classNode) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    public static String referenceClassDesc(String className) {
        return "L" + className.replace('.', '/') + ";";
    }
}
