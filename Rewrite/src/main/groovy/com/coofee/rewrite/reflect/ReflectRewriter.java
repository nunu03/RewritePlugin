package com.coofee.rewrite.reflect;

import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.TransformInvocation;
import com.coofee.rewrite.Rewriter;

import org.apache.commons.io.FileUtils;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.IOException;

public class ReflectRewriter extends Rewriter.Adapter {

    private static final String RESULT_FILE_NAME = "reflect.json";

    private final File rootFolder;

    private final ReflectExtension reflectExtension;

    private final ReflectCollector collector;

    public ReflectRewriter(File rootFolder, ReflectExtension reflectExtension) {
        this.rootFolder = rootFolder;
        this.reflectExtension = reflectExtension;
        this.collector = new ReflectCollector(reflectExtension.classAndMethods);
    }

    @Override
    public ClassNode transform(QualifiedContent input, ClassNode classNode) {
        collector.transform(input.getName(), classNode);
        return classNode;
    }

    @Override
    public void postTransform(TransformInvocation transformInvocation) {
        ReflectResult result = ReflectResult.generate(reflectExtension.myAppPackages, collector.reflectInfoList);
        File resultFile = new File(rootFolder, RESULT_FILE_NAME);
        try {
            FileUtils.writeStringToFile(resultFile, result.toJson());
        } catch (IOException e) {
            System.out.println("[RewritePlugin] reflect fail write to file " + resultFile.getAbsolutePath());
            e.printStackTrace();
        }
    }

}
