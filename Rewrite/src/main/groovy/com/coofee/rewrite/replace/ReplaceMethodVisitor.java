package com.coofee.rewrite.replace;

import com.coofee.rewrite.hierarchy.ClassHierarchy;
import com.coofee.rewrite.hierarchy.ClassNode;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.util.HashSet;
import java.util.Set;

public class ReplaceMethodVisitor extends GeneratorAdapter {

    public ReplaceMethodVisitor(int api, MethodVisitor methodVisitor, int access, String name, String descriptor) {
        super(api, methodVisitor, access, name, descriptor);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        System.out.println("owner=" + owner + ", name=" + name + ", descriptor=" + descriptor + ", isInterface=" + isInterface);

        ClassNode classNode = ClassHierarchy.get(owner);

        if (classNode == null) {
            System.err.println("cannot find class node for owner=" + owner);
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);

        } else if (!isInterface
                && classNode.isSubclass("com/coofee/rewrite/Rewriter")
                && "transform".equals(name)
                && "(Lcom/android/build/api/transform/QualifiedContent;Lorg/objectweb/asm/tree/ClassNode;)Lorg/objectweb/asm/tree/ClassNode;".equals(descriptor)) {

            System.out.println("replace method of owner=" + owner);

            String shadowOwner = "com/coofee/rewrite/Shadow";
            String shadowName = "transform";
            String shadowDesc = "(Lcom/coofee/rewrite/Rewriter;Lcom/android/build/api/transform/QualifiedContent;Lorg/objectweb/asm/tree/ClassNode;)Lorg/objectweb/asm/tree/ClassNode;";
            super.visitMethodInsn(Opcodes.INVOKESTATIC, shadowOwner, shadowName, shadowDesc, isInterface);

        } else {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }
    }
}
