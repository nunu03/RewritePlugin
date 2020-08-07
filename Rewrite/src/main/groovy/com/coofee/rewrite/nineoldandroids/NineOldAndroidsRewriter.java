package com.coofee.rewrite.nineoldandroids;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.TransformInvocation;
import com.coofee.rewrite.Rewriter;

import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class NineOldAndroidsRewriter extends Rewriter.Adapter {

    private final NineOldAndroidsExtension config;

    private final Set<JarInput> nineOldAndroidsJarInputSet = new HashSet<>();

    private Remapper remapper;

    public NineOldAndroidsRewriter(NineOldAndroidsExtension nineOldAndroidsExtension) {
        this.config = nineOldAndroidsExtension;
    }

    private static Map<String, String> generateClassMapper(File originJar) throws IOException {
        Map<String, String> classMapper = new HashMap<>(100);
        ZipFile origin = new ZipFile(originJar);
        Enumeration<? extends ZipEntry> originEntries = origin.entries();
        while (originEntries.hasMoreElements()) {

            ZipEntry originEntry = originEntries.nextElement();
            final String name = originEntry.getName();
            if (!name.endsWith(".class")) {
                continue;
            }

            if (name.startsWith("com/nineoldandroids/")) {
                String newName = name.replace("com/nineoldandroids", "android");
                classMapper.put(convertEntryClassNameToDescriptor(name), convertEntryClassNameToDescriptor(newName));
            }
        }

        System.out.println("[RewritePlugin] generateClassMapper is " + classMapper);
        return classMapper;
    }

    private static String convertEntryClassNameToDescriptor(String entryClassName) {
        int classSuffixIndex = entryClassName.length() - ".class".length();
        return entryClassName.substring(0, classSuffixIndex);
//        return "L" + entryClassName.substring(0, classSuffixIndex) + ";";
    }

    @Override
    public void preTransform(TransformInvocation transformInvocation) {
        transformInvocation.getInputs().forEach(transformInput -> {
            transformInput.getJarInputs().forEach(jarInput -> {
                System.out.println("[RewritePlugin] preTransform jarInput name=" + jarInput.getName() + ", file=" + jarInput.getFile());
                if (jarInput.getName().startsWith("com.nineoldandroids:")) {
                    nineOldAndroidsJarInputSet.add(jarInput);
                }
            });
        });

        final Map<String, String> classMapping = new HashMap<>();
        nineOldAndroidsJarInputSet.forEach(jarInput -> {
            try {
                classMapping.putAll(generateClassMapper(jarInput.getFile()));
            } catch (IOException e) {
                System.out.println("[RewritePlugin] preTransform fail generateClassMapper file=" + jarInput.getFile());
                e.printStackTrace();
            }
        });

        remapper = new SimpleRemapper(classMapping);
    }

    @Override
    public boolean needRemoveDependency(JarInput jarInput, DirectoryInput directoryInput) {
        return nineOldAndroidsJarInputSet.contains(jarInput);
    }

    @Override
    public ClassNode transform(QualifiedContent input, ClassNode classNode) {
        Set<String> excludes = config.excludes;
        if (excludes != null) {
            for (String groupName : excludes) {
                if (input.getName().startsWith(groupName)) {
                    return classNode;
                }
            }
        }

        ClassNode output = new ClassNode();
        ClassRemapper classRemapper = new ClassRemapper(output, remapper);
        classNode.accept(classRemapper);
        return output;
    }

}
