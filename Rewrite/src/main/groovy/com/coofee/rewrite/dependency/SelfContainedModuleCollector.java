package com.coofee.rewrite.dependency;

import com.android.build.api.transform.TransformInvocation;
import com.coofee.rewrite.Rewriter;
import com.coofee.rewrite.util.AsmUtil;
import com.coofee.rewrite.util.ClassUtil;
import com.coofee.rewrite.util.FileUtil;
import com.google.gson.Gson;

import org.apache.commons.io.FileUtils;
import org.apache.maven.shared.dependency.analyzer.asm.ASMDependencyAnalyzer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SelfContainedModuleCollector extends Rewriter.Adapter {

    private static final String RESULT_FILE_NAME = "dependency.self.contained.json";

    private final ASMDependencyAnalyzer analyzer = new ASMDependencyAnalyzer();

    private final Set<String> selfContainedModule = new HashSet<>();

    private final File rootFolder;

    public SelfContainedModuleCollector(File rootFolder) {
        this.rootFolder = rootFolder;
    }

    @Override
    public void preTransform(TransformInvocation transformInvocation) {
        super.preTransform(transformInvocation);

        final Map<String, Set<String>> moduleAndClassMap = new HashMap<>();
        final Map<String, Set<String>> moduleReferencesMap = new HashMap<>();

        transformInvocation.getInputs().forEach(inputs -> {

            inputs.getJarInputs().forEach(jarInput -> {

                try {
                    final Set<String> classDescSet = new HashSet<>();
                    FileUtil.traverseJarClass(jarInput.getFile(), bytecode -> {
                        classDescSet.add(AsmUtil.getClassName(bytecode));

                    });

                    if (!classDescSet.isEmpty()) {
                        moduleAndClassMap.put(jarInput.getName(), ClassUtil.convert(classDescSet));
                        System.out.println("[RewritePlugin] self contained collector; " + jarInput.getName() + " contains " + classDescSet.size() + " class.");

                        moduleReferencesMap.put(jarInput.getName(), analyzer.analyze(jarInput.getFile().toURL()));
                    }
                } catch (Throwable e) {
                    System.out.println("[RewritePlugin] self contained collector fail; jarInput=" + jarInput);
                    e.printStackTrace();
                }
            });

            inputs.getDirectoryInputs().forEach(directoryInput -> {

//                final Set<String> classDescSet = new HashSet<>();
//                FileUtil.eachFileRecurse(directoryInput.getFile(), inputFile -> {
//                    try {
//                        byte[] bytecode = FileUtils.readFileToByteArray(inputFile);
//                        if (ClassUtil.isValidClassBytes(bytecode)) {
//                            classDescSet.add(AsmUtil.getClassName(bytecode));
//                        }
//                    } catch (IOException e) {
//                        System.out.println("[RewritePlugin] self contained collector fail; directoryInput=" + directoryInput + ", file=" + inputFile);
//                        e.printStackTrace();
//                    }
//
//                });
//                moduleAndClassMap.put(directoryInput.getName(), ClassUtil.convert(classDescSet));
//                System.out.println("[RewritePlugin] self contained collector; " + directoryInput.getName() + " contains " + classDescSet.size() + " class.");

                try {
                    moduleReferencesMap.put(directoryInput.getName(), analyzer.analyze(directoryInput.getFile().toURL()));
                } catch (IOException e) {
                    System.out.println("[RewritePlugin] self contained collector fail analyze directoryInput=" + directoryInput);
                    e.printStackTrace();
                }
            });
        });

        for (Map.Entry<String, Set<String>> entry : moduleAndClassMap.entrySet()) {
            String module = entry.getKey();
            Set<String> moduleClassSet = entry.getValue();

            int intersectionSize = 0;
            for (Map.Entry<String, Set<String>> referenceEntry : moduleReferencesMap.entrySet()) {
                if (module.equals(referenceEntry.getKey())) {
                    continue;
                }


                intersectionSize = intersectionSize(moduleClassSet, referenceEntry.getValue());
                if (intersectionSize > 0) {
                    break;
                }
            }

            if (intersectionSize == 0) {
                selfContainedModule.add(module);
            }
        }

        // TODO exclude specific module;

        // TODO how to solve custom view reference in xml layout?

        System.out.println("[RewritePlugin] self contained collector modules=" + selfContainedModule);
    }

    @Override
    public void postTransform(TransformInvocation transformInvocation) {
        super.postTransform(transformInvocation);

        File resultFile = new File(rootFolder, RESULT_FILE_NAME);
        try {
            FileUtils.writeStringToFile(resultFile, new Gson().toJson(selfContainedModule));
        } catch (IOException e) {
            System.out.println("[RewritePlugin] self contained collector fail write to file " + resultFile.getAbsolutePath());
            e.printStackTrace();
        }
    }

    private static int intersectionSize(Set<String> set1, Set<String> set2) {
        if (set1 == null || set2 == null) {
            return 0;
        }

        int size = 0;
        for (String s : set1) {
            if (set2.contains(s)) {
                size++;
            }
        }

        return size;
    }
}
