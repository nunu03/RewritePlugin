package com.coofee.rewrite.reflect;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.ArrayList;
import java.util.List;

public class StackOperands {

    public static final int NOT_FOUND = -1;

    private final MethodInsnNode insnNode;

    public StackOperands(MethodInsnNode insnNode) {
        this.insnNode = insnNode;
    }

    private static AbstractInsnNode skipLineNumberOrLabelNode(AbstractInsnNode insnNode) {
        AbstractInsnNode previous = insnNode.getPrevious();
        while (previous instanceof LineNumberNode || previous instanceof LabelNode) {
            previous = previous.getPrevious();
        }
        return previous;
    }

    private static List<String> resolveProbablyClassList(MethodInsnNode insnNode) {
        List<String> probablyClassList = new ArrayList<>();

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

        String operand = resolveOneStringOperand();

        if (operand == null) {
            resolveResult.exact = false;
            resolveResult.probablyClassList.addAll(resolveProbablyClassList(insnNode));
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
