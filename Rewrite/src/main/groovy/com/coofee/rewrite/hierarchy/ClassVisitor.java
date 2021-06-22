package com.coofee.rewrite.hierarchy;

import com.coofee.rewrite.replace.ReplaceMethodVisitor;
import org.objectweb.asm.MethodVisitor;

public class ClassVisitor extends org.objectweb.asm.ClassVisitor {

    public ClassVisitor(int api, org.objectweb.asm.ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        ClassNode classNode = new ClassNode(access, name, signature, superName, interfaces);
        System.out.println("access=" + access + ", name=" + name + ", signature=" + signature + ", superName=" + superName + ", interfaces=" + java.util.Arrays.toString(interfaces) + ", node=" + classNode);
        ClassHierarchy.add(classNode);

//        while (superName != null && superName.length() > 0) {
//            ClassNode superNameNode = ClassHierarchy.get(superName);
//            if (superNameNode == null) {
//            }
//        }
    }
}
