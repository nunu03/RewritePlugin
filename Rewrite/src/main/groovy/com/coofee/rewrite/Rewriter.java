package com.coofee.rewrite;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.TransformInvocation;

import org.objectweb.asm.tree.ClassNode;

public interface Rewriter {
    void preTransform(TransformInvocation transformInvocation);

    boolean needRemoveDependency(JarInput jarInput, DirectoryInput directoryInput);

    ClassNode transform(QualifiedContent input, ClassNode classNode);

    void postTransform(TransformInvocation transformInvocation);

    class Adapter implements Rewriter {

        @Override
        public void preTransform(TransformInvocation transformInvocation) {

        }

        @Override
        public boolean needRemoveDependency(JarInput jarInput, DirectoryInput directoryInput) {
            return false;
        }

        @Override
        public ClassNode transform(QualifiedContent input, ClassNode classNode) {
            return classNode;
        }

        @Override
        public void postTransform(TransformInvocation transformInvocation) {

        }
    }
}
