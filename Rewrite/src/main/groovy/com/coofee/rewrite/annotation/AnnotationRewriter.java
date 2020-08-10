package com.coofee.rewrite.annotation;

import com.android.build.api.transform.QualifiedContent;
import com.coofee.rewrite.Rewriter;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class AnnotationRewriter extends Rewriter.Adapter {

    private final AnnotationExtension annotationExtension;

    public AnnotationRewriter(AnnotationExtension annotationExtension) {
        this.annotationExtension = annotationExtension;
    }

    private static int removeAnnotations(ClassNode classNode, Set<String> annotations) {
        int removedCount = 0;

        removedCount += removeAnnotations(classNode.visibleAnnotations, annotations);
        removedCount += removeAnnotations(classNode.invisibleAnnotations, annotations);

        List<MethodNode> methodNodes = classNode.methods;
        if (methodNodes != null && !methodNodes.isEmpty()) {
            for (MethodNode methodNode : methodNodes) {
                removedCount += removeAnnotations(methodNode.visibleAnnotations, annotations);
                removedCount += removeAnnotations(methodNode.invisibleAnnotations, annotations);
            }
        }

        List<FieldNode> fieldNodes = classNode.fields;
        if (fieldNodes != null && !fieldNodes.isEmpty()) {
            for (FieldNode fieldNode : fieldNodes) {
                removedCount += removeAnnotations(fieldNode.invisibleAnnotations, annotations);
                removedCount += removeAnnotations(fieldNode.visibleAnnotations, annotations);
            }
        }

        return removedCount;
    }

    @Override
    public ClassNode transform(QualifiedContent input, ClassNode classNode) {
        if (annotationExtension.excludes != null && !annotationExtension.excludes.isEmpty()) {
            int removedCount = removeAnnotations(classNode, annotationExtension.excludes);
            if (removedCount > 0) {
                System.out.println("[RewritePlugin] annotation rewriter removed " + removedCount + " annotation from " + classNode.name);
            }
        }
        return classNode;
    }

    private static int removeAnnotations(List<AnnotationNode> annotationNodes, Set<String> annotations) {
        if (annotationNodes == null || annotationNodes.isEmpty()) {
//            return annotationNodes;
            return 0;
        }

        int removedCount = 0;
        Iterator<AnnotationNode> iterator = annotationNodes.iterator();
        while (iterator.hasNext()) {
            if (annotations.contains(iterator.next().desc)) {
                iterator.remove();
                removedCount++;
            }
        }

        return removedCount;
    }
}
