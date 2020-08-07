package com.coofee.rewrite.reflect;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;

import java.util.ArrayList;
import java.util.List;

public class StackOperands {

    public static final int NOT_FOUND = -1;

    private final MethodInsnNode insnNode;

    private final Frame<BasicValue>[] frames;

    private final int index;

    public StackOperands(MethodInsnNode insnNode, Frame<BasicValue>[] frames, int index) {
        this.insnNode = insnNode;
        this.frames = frames;
        this.index = index;
    }

    private static AbstractInsnNode skipLineNumberOrLabelNode(AbstractInsnNode insnNode) {
        AbstractInsnNode previous = insnNode.getPrevious();
        while (previous instanceof LineNumberNode || previous instanceof LabelNode) {
            previous = previous.getPrevious();
        }
        return previous;
    }

    private static List<String> resolveProbablyClassList(MethodInsnNode insnNode, Frame<BasicValue>[] frames, int index) {
        List<String> probablyClassList = new ArrayList<>();

        final Frame<BasicValue> currentFrame = frames[index];
        final int stackSize = currentFrame.getStackSize();
//        System.out.println("stackSize=" + stackSize);

        for (AbstractInsnNode previous = insnNode.getPrevious();
             previous != null;
             previous = previous.getPrevious()) {

            if (previous instanceof LdcInsnNode) {
                LdcInsnNode ldcInsnNode = (LdcInsnNode) previous;
                if (ldcInsnNode.cst instanceof String) {
                    probablyClassList.add((String) ldcInsnNode.cst);
                }
            }
        }

        return probablyClassList;
    }

    public ResolveResult resolve() {
        ResolveResult resolveResult = new ResolveResult();
        resolveResult.probablyClassList = new ArrayList<>();


        final int stackSize = frames[index].getStackSize();
//        System.out.println("stackSize=" + stackSize);

//        String operand = null;
//        if (stackSize == 1 || stackSize == 2) {
//            operand = resolveOneStringOperand();
//        }

        String operand = resolveOneStringOperand();

        if (operand == null) {
            resolveResult.exact = false;
            resolveResult.probablyClassList.addAll(resolveProbablyClassList(insnNode, frames, index));
        } else {
            resolveResult.exact = true;
            resolveResult.probablyClassList.add(operand);
        }


        return resolveResult;
    }

    private String resolveOneStringOperand() {
        AbstractInsnNode previous = skipLineNumberOrLabelNode(insnNode);

        if (previous.getOpcode() == Opcodes.LDC) {
            final LdcInsnNode ldc = (LdcInsnNode) previous;
            if (ldc.cst instanceof String) {
                return (String) ldc.cst;
            }
        }

        int var = NOT_FOUND;
        for (; previous != null; previous = previous.getPrevious()) {

            final int opcode = previous.getOpcode();
            switch (opcode) {
                case Opcodes.ALOAD:
                    final VarInsnNode aload = (VarInsnNode) previous;
                    var = aload.var;
                    break;

                case Opcodes.ASTORE:
                    final VarInsnNode astore = (VarInsnNode) previous;
                    if (var == astore.var) {
                        AbstractInsnNode testLdc = skipLineNumberOrLabelNode(astore);
                        if (testLdc instanceof LdcInsnNode) {
                            final LdcInsnNode ldcInsnNode = (LdcInsnNode) testLdc;
                            if (ldcInsnNode.cst instanceof String) {
                                return (String) ldcInsnNode.cst;
                            }
                        }
                    }
                    break;

                default:
                    // ignore
                    break;
            }
        }

        return null;
    }

    public static class ResolveResult {
        public boolean exact;

        public List<String> probablyClassList;
    }
}
