package com.coofee.rewrite;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Status;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.build.gradle.internal.pipeline.IntermediateFolderUtils;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.coofee.rewrite.annotation.AnnotationExtension;
import com.coofee.rewrite.annotation.AnnotationRewriter;
import com.coofee.rewrite.nineoldandroids.NineOldAndroidsExtension;
import com.coofee.rewrite.nineoldandroids.NineOldAndroidsRewriter;
import com.coofee.rewrite.reflect.ReflectExtension;
import com.coofee.rewrite.reflect.ReflectRewriter;
import com.coofee.rewrite.util.AsmUtil;
import com.coofee.rewrite.util.ClassUtil;
import com.coofee.rewrite.util.FileUtil;
import com.coofee.rewrite.util.ReflectUtil;

import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class RewriteTransform extends Transform {

    private final Project project;
    private final Set<Rewriter> rewriterSet = new HashSet<>();
//    private RewriteCache rewriteCache;

    public RewriteTransform(Project project) {
        this.project = project;

        final RewriteExtension rewriteExtension = (RewriteExtension) project.getExtensions().getByName("rewrite");
        System.out.println("[RewritePlugin] rewriteExtension=" + rewriteExtension);
    }

    @Override
    public String getName() {
        return "rewrite";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return true;
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws IOException {
        final TransformOutputProvider outputProvider = transformInvocation.getOutputProvider();

        final IntermediateFolderUtils folderUtils = ReflectUtil.getFieldValue(outputProvider, "folderUtils");
        System.out.println("[RewritePlugin] rootFolder=" + folderUtils.getRootFolder());

        final RewriteExtension rewriteExtension = (RewriteExtension) project.getExtensions().getByName("rewrite");
        System.out.println("[RewritePlugin] rewriteExtension=" + rewriteExtension);

        NineOldAndroidsExtension nineOldAndroids = rewriteExtension.nineOldAndroids;
        System.out.println("[RewritePlugin] nineOldAndroids=" + nineOldAndroids);
        if (nineOldAndroids != null && nineOldAndroids.enable) {
            rewriterSet.add(new NineOldAndroidsRewriter(nineOldAndroids));
        }

        ReflectExtension reflect = rewriteExtension.reflect;
        System.out.println("[RewritePlugin] reflect=" + reflect);
        if (reflect != null && reflect.enable) {
            rewriterSet.add(new ReflectRewriter(folderUtils.getRootFolder(), reflect));
        }

        AnnotationExtension annotation = rewriteExtension.annotation;
        System.out.println("[RewritePlugin] annotation=" + annotation);
        if (annotation != null && annotation.enable) {
            rewriterSet.add(new AnnotationRewriter(annotation));
        }

//        final File cacheFile = new File(folderUtils.getRootFolder(), RewriteCache.CACHE_FILE_NAME);
//        rewriteCache = new RewriteCache(cacheFile, rewriteExtension);
//        boolean valid = rewriteCache.isValid();
//        System.out.println("[RewritePlugin] load cache success, cache is valid? " + valid);

        for (Rewriter rewriter : rewriterSet) {
            rewriter.preTransform(transformInvocation);
        }

        final boolean incremental = transformInvocation.isIncremental();
        System.out.println("[RewritePlugin] transformInvocation.isIncremental()=" + incremental);

        if (incremental) {
            System.out.println("[RewritePlugin] incremental transform");
            incrementTransform(transformInvocation, outputProvider);
        } else {
            System.out.println("[RewritePlugin] full transform");
            fullTransform(transformInvocation, outputProvider);
        }

        for (Rewriter rewriter : rewriterSet) {
            rewriter.postTransform(transformInvocation);
        }

//        boolean commit = rewriteCache.commit();
//        System.out.println("[RewritePlugin] commit to cache success? " + commit);
    }

    @Override
    public boolean isCacheable() {
        return true;
    }

    private void fullTransform(TransformInvocation transformInvocation, TransformOutputProvider outputProvider) throws IOException {
        outputProvider.deleteAll();

        transformInvocation.getInputs().forEach(transformInput -> {

            // 处理jar
            transformInput.getJarInputs().forEach(jarInput -> {
                processJar(outputProvider, jarInput);
            });

            // 处理directory
            transformInput.getDirectoryInputs().forEach(directoryInput -> {

                File inputDir = directoryInput.getFile();
                File outputDir = outputProvider.getContentLocation(directoryInput.getName(), directoryInput.getContentTypes(), directoryInput.getScopes(), Format.DIRECTORY);

                for (Rewriter rewriter : rewriterSet) {
                    if (rewriter.needRemoveDependency(null, directoryInput)) {
                        System.out.println("[RewritePlugin] remove jar: " + directoryInput.getName() + ", file=" + directoryInput.getName());
                        FileUtils.deleteQuietly(outputDir);
                        return;
                    }
                }

                System.out.println("[RewritePlugin] process directory: " + inputDir.getName());
                FileUtil.eachFileRecurse(inputDir, inputFile -> {
                    String outFilePath = inputFile.getAbsolutePath().replace(inputDir.getAbsolutePath(), outputDir.getAbsolutePath());
                    File outputFile = new File(outFilePath);
                    processFile(directoryInput, inputFile, outputFile);
                });
            });
        });
    }

    private void incrementTransform(TransformInvocation transformInvocation, TransformOutputProvider outputProvider) {
        transformInvocation.getInputs().forEach(transformInput -> {

            // 处理jar
            transformInput.getJarInputs().forEach(jarInput -> {
                // 需要删除 com.nineoldandroids库

                File outLocation = outputProvider.getContentLocation(jarInput.getName(), jarInput.getContentTypes(), jarInput.getScopes(), Format.JAR);
                System.out.println("[RewritePlugin] increment jarInput=" + jarInput.getName() + ", status=" + jarInput.getStatus());

                switch (jarInput.getStatus()) {
                    case ADDED:
                    case CHANGED:
                        processJar(outputProvider, jarInput);
                        break;

                    case REMOVED:
                        FileUtils.deleteQuietly(outLocation);
                        break;

                    case NOTCHANGED:
                    default:
                        break;
                }

            });

            // 处理directory
            transformInput.getDirectoryInputs().forEach(directoryInput -> {

                File inputDir = directoryInput.getFile();
                File outputDir = outputProvider.getContentLocation(directoryInput.getName(), directoryInput.getContentTypes(), directoryInput.getScopes(), Format.DIRECTORY);
                Map<File, Status> changedFiles = directoryInput.getChangedFiles();
                if (changedFiles == null || changedFiles.isEmpty()) {
                    return;
                }

                System.out.println("[RewritePlugin] increment directoryInput=" + directoryInput.getName() + ", changedFiles.size=" + changedFiles.size());

                changedFiles.forEach((inputFile, status) -> {
                    String outFilePath = inputFile.getAbsolutePath().replace(inputDir.getAbsolutePath(), outputDir.getAbsolutePath());
                    File outputFile = new File(outFilePath);

                    System.out.println("[RewritePlugin] changed file=" + inputFile + ", status=" + status);

                    switch (status) {
                        case ADDED:
                        case CHANGED:
                            processFile(directoryInput, inputFile, outputFile);
                            break;

                        case REMOVED:
                            FileUtils.deleteQuietly(outputFile);
                            break;

                        case NOTCHANGED:
                        default:
                            break;
                    }
                });
            });
        });
    }

    private void processFile(DirectoryInput directoryInput, File inputFile, File outputFile) {
        FileUtils.deleteQuietly(outputFile);

        try {
            byte[] bytecode = FileUtils.readFileToByteArray(inputFile);
            if (ClassUtil.isValidClassBytes(bytecode)) {
                ClassNode classNode = AsmUtil.convert(bytecode);
                for (Rewriter rewriter : rewriterSet) {
                    classNode = rewriter.transform(directoryInput, classNode);
                }
                bytecode = AsmUtil.convert(classNode);
            }
            FileUtils.writeByteArrayToFile(outputFile, bytecode);

//                        rewriteCache.put(directoryInput.getName(), directoryInput.getFile());
        } catch (Throwable e) {
            project.getLogger().info("[RewritePlugin] fail process file: " + inputFile, e);
        }
    }

    private void processJar(TransformOutputProvider outputProvider, JarInput jarInput) {
        File outputJar = outputProvider.getContentLocation(jarInput.getName(), jarInput.getContentTypes(), jarInput.getScopes(), Format.JAR);

        for (Rewriter rewriter : rewriterSet) {
            if (rewriter.needRemoveDependency(jarInput, null)) {
                System.out.println("[RewritePlugin] remove jar: " + jarInput.getName() + ", file=" + outputJar.getName());
                FileUtils.deleteQuietly(outputJar);
                return;
            }
        }

        System.out.println("[RewritePlugin] process jar: " + jarInput.getName());
        try {
            FileUtil.traverseJarClass(jarInput.getFile(), outputJar, (name, bytecode) -> {
                ClassNode classNode = AsmUtil.convert(bytecode);
                for (Rewriter rewriter : rewriterSet) {
                    classNode = rewriter.transform(jarInput, classNode);
                }
                return AsmUtil.convert(classNode);
            });

//            rewriteCache.put(jarInput.getName(), jarInput.getFile());
        } catch (Throwable e) {
            project.getLogger().info("[RewritePlugin] fail process jar: " + jarInput.getFile(), e);
        }
    }
}