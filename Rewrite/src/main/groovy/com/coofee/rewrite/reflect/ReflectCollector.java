package com.coofee.rewrite.reflect;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.objectweb.asm.Opcodes.ARRAYLENGTH;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.MONITORENTER;
import static org.objectweb.asm.Opcodes.MONITOREXIT;
import static org.objectweb.asm.Opcodes.PUTFIELD;

public class ReflectCollector {

    public final Map<String, Set<String>> classAndMethods;
    public final List<ReflectInfo> reflectInfoList = new ArrayList<>();

    public ReflectCollector(Map<String, Set<String>> classAndMethods) {
        this.classAndMethods = classAndMethods;
    }

    private static int getLineNumber(MethodInsnNode insnNode) {
        for (AbstractInsnNode previous = insnNode.getPrevious();
             previous != null;
             previous = previous.getPrevious()) {

            if (previous instanceof LineNumberNode) {
                return ((LineNumberNode) previous).line;
            }
        }

        return -1;
    }

    private static void printInfo(MethodInsnNode insnNode, Frame<BasicValue>[] frames, int insnIndex) {
        if (frames == null) {
            return;
        }

        Frame<BasicValue> frame = frames[insnIndex];


        final BasicValue target = getTarget(insnNode, frame);
        if (target != null) {
            System.out.println("target=" + target);
        }

        final int locals = frame.getLocals();
        System.out.println("local.size=" + locals);
        for (int i = 0; i < locals; i++) {
            final BasicValue local = frame.getLocal(i);
//            System.out.println("local=" + local);
        }

        final int stackSize = frame.getStackSize();
        System.out.println("stackSize=" + stackSize);
        for (int i = 0; i < stackSize; i++) {
            final BasicValue stackOperand = frame.getStack(i);
//            System.out.println("stack.operand=" + stackOperand);
        }

    }

    private static BasicValue getTarget(AbstractInsnNode insn, Frame<BasicValue> f) {
        switch (insn.getOpcode()) {
            case GETFIELD:
            case ARRAYLENGTH:
            case MONITORENTER:
            case MONITOREXIT:
                return getStackValue(f, 0);
            case PUTFIELD:
                return getStackValue(f, 1);
            case INVOKEVIRTUAL:
            case INVOKESPECIAL:
            case INVOKEINTERFACE:
                String desc = ((MethodInsnNode) insn).desc;
                return getStackValue(f, Type.getArgumentTypes(desc).length);
            default:
                // ignore
                break;
        }
        return null;
    }

    private static BasicValue getStackValue(Frame<BasicValue> f, int index) {
        int top = f.getStackSize() - 1;
        return index <= top ? f.getStack(top - index) : null;
    }

    public ClassNode transform(String moduleName, ClassNode input) {
        if (input.methods == null || input.methods.isEmpty()) {
            return input;
        }

        for (MethodNode methodNode : input.methods) {
            final AbstractInsnNode[] abstractInsnNodes = methodNode.instructions.toArray();
            for (int index = 0; index < abstractInsnNodes.length; index++) {
                if (!(abstractInsnNodes[index] instanceof MethodInsnNode)) {
                    continue;
                }

                final MethodInsnNode methodInsnNode = ((MethodInsnNode) abstractInsnNodes[index]);
                int opcode = methodInsnNode.getOpcode();
                if (opcode == INVOKESTATIC || opcode == INVOKEVIRTUAL) {
                    String owner = methodInsnNode.owner;
                    Set<String> methods = classAndMethods.get(owner);

                    if (methods != null && methods.contains(methodInsnNode.name)) {
                        ReflectInfo reflectInfo = new ReflectInfo();
                        reflectInfo.moduleName = moduleName;
                        reflectInfo.reflectInfo = owner.substring(owner.lastIndexOf('/') + 1) + "." + methodInsnNode.name + "()";
                        reflectInfo.className = input.name;
                        reflectInfo.methodName = methodNode.name;
                        reflectInfo.lineNumber = getLineNumber(methodInsnNode);
                        reflectInfo.methodDesc = methodNode.desc;
                        final StackOperands.ResolveResult resolveResult = new StackOperands(methodInsnNode).resolve();
                        reflectInfo.exact = resolveResult.exact;
                        reflectInfo.probablyClassList = resolveResult.probablyClassList;
                        System.out.println(reflectInfo.format());
//                    printInfo(methodInsnNode, frames, index);
                        reflectInfoList.add(reflectInfo);
                    }
                }
            }
        }

        return input;
    }

    public static class ReflectInfo {
        public String moduleName;

        public String reflectInfo;

        public String className;

        public String methodName;

        public String methodDesc;

        public int lineNumber;

        public boolean exact;

        public List<String> probablyClassList;

        public String format() {
            return String.format("%s accessed by %s.%s(#%d) in %s, method.desc=%s, exact=%s, probablyClassList=%s", reflectInfo, className, methodName, lineNumber, moduleName, methodDesc, exact, probablyClassList);
        }
    }
}
