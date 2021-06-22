package com.coofee.rewrite.method;

import com.android.build.api.transform.QualifiedContent;
import com.coofee.rewrite.Rewriter;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.TraceMethodVisitor;

public class MethodRewriter extends Rewriter.Adapter {

    @Override
    public ClassNode transform(QualifiedContent input, ClassNode classNode) {

        return super.transform(input, classNode);
    }
}
