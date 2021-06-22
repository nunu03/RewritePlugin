package com.coofee.rewrite.hierarchy;

import com.coofee.rewrite.util.AsmUtil;
import com.coofee.rewrite.util.FileUtil;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;

public class Test {

    public static void main(String[] args) throws IOException {
        System.out.println(Number.class.isAssignableFrom(Integer.class));

        Method getSharedPreferences = Method.getMethod("android.content.SharedPreferences getSharedPreferences(java.lang.String, int)");
        System.out.println("getSharedPreferences.name=" + getSharedPreferences.getName() + ", descriptor=" + getSharedPreferences.getDescriptor());


        // com.coofee.rewrite.Rewriter
        Method method = Method.getMethod("org.objectweb.asm.tree.ClassNode transform(com.android.build.api.transform.QualifiedContent, org.objectweb.asm.tree.ClassNode)");
        System.out.println("name=" + method.getName() + ", descriptor=" + method.getDescriptor() + ", returnType=" + method.getReturnType() + ", argumentTypes=" + Arrays.toString(method.getArgumentTypes()));

        File srcDir = new File("Rewrite/build/classes");
        File destDir = new File("Rewrite/build/rewrite");
        FileUtils.deleteDirectory(destDir);
        destDir.mkdirs();
        System.out.println("srcDir=" + srcDir.getAbsolutePath() + ", destDir=" + destDir);

        FileUtil.eachFileRecurse(srcDir, new Consumer<File>() {
            @Override
            public void accept(File file) {
                try {
                    ClassNode convert = AsmUtil.convert(FileUtils.readFileToByteArray(file));
                    ClassHierarchy.add(new com.coofee.rewrite.hierarchy.ClassNode(convert.access, convert.name, convert.signature, convert.superName, convert.interfaces));

                    List<MethodNode> methods = convert.methods;
                    for (MethodNode methodNode : methods) {
                        InsnList instructions = methodNode.instructions;
                        ListIterator<AbstractInsnNode> iterator = instructions.iterator();
                        while (iterator.hasNext()) {
                            AbstractInsnNode next = iterator.next();
                            if (next instanceof MethodInsnNode) {
                                MethodInsnNode insnNode = (MethodInsnNode) next;

                                if ("transform".equals(insnNode.name)
                                        && "(Lcom/android/build/api/transform/QualifiedContent;Lorg/objectweb/asm/tree/ClassNode;)Lorg/objectweb/asm/tree/ClassNode;".equals(insnNode.desc)
                                ) {
                                    if (!ClassHierarchy.contains(insnNode.owner)) {
                                        System.out.println("replace not contains " + insnNode.owner);
                                    }

                                    System.out.println("replace matched owner=" + insnNode.owner);
                                }

                                com.coofee.rewrite.hierarchy.ClassNode classNode = ClassHierarchy.get(insnNode.owner);
                                if (classNode != null && classNode.isSubclass("com/coofee/rewrite/Rewriter")
                                        && "transform".equals(insnNode.name)
                                        && "(Lcom/android/build/api/transform/QualifiedContent;Lorg/objectweb/asm/tree/ClassNode;)Lorg/objectweb/asm/tree/ClassNode;".equals(insnNode.desc)
                                ) {
                                    System.out.println("replace method of owner=" + insnNode.owner);
                                    String shadowOwner = "com/coofee/rewrite/Shadow";
                                    String shadowName = "transform";
                                    String shadowDesc = "(Lcom/coofee/rewrite/Rewriter;Lcom/android/build/api/transform/QualifiedContent;Lorg/objectweb/asm/tree/ClassNode;)Lorg/objectweb/asm/tree/ClassNode;";
                                    insnNode.setOpcode(Opcodes.INVOKESTATIC);
                                    insnNode.owner = shadowOwner;
                                    insnNode.name = shadowName;
                                    insnNode.desc = shadowDesc;
                                }
                            }
                        }
                    }
                    byte[] bytecode = AsmUtil.convert(convert);
                    File dest = new File(file.getAbsolutePath().replace(srcDir.getAbsolutePath(), destDir.getAbsolutePath()));
                    FileUtils.writeByteArrayToFile(dest, bytecode);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        boolean assignableFrom = ClassHierarchy.get("com/coofee/rewrite/Rewriter").isAssignableFrom("com/coofee/rewrite/method/MethodRewriter");
        System.out.println("assignableFrom=" + assignableFrom);
        System.out.println(ClassHierarchy.getAll());
    }
}
