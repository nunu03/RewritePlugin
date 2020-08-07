package com.coofee.rewrite.nineoldandroids

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionAdapter
import org.gradle.api.tasks.TaskState
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.Remapper
import org.objectweb.asm.commons.SimpleRemapper

import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class ProguardHook {
    Project project

    ProguardHook(Project project) {
        this.project = project
    }

    private void hook() {
        project.gradle.taskGraph.addTaskExecutionListener(new TaskExecutionAdapter() {
            @Override
            public void afterExecute(Task task, TaskState state) {
                super.afterExecute(task, state);

                if (task.getName().startsWith("transformClassesAndResourcesWithProguardFor")) {
//                    println("${task.properties}")

                    try {
                        File jarDir = task.properties['streamOutputFolder']
                        File originJar = new File(jarDir, "0.jar");
                        File targetJar = new File(originJar.getParentFile(), "target.jar");
                        Map<String, String> classMapping = NineOldAndroidsRewriter.generateClassMapper(originJar)
                        NineOldAndroidsRewriter.processJar(originJar, targetJar, classMapping);
                        originJar.delete();
                        targetJar.renameTo(originJar);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static void processJar(File originJar, File targetJar, Map<String, String> mapping) throws IOException {
        if (targetJar.exists()) {
            targetJar.delete();
        }

        final SimpleRemapper simpleRemapper = new SimpleRemapper(mapping);

        ZipFile origin = new ZipFile(originJar);
        ZipOutputStream targetZipOut = new ZipOutputStream(new FileOutputStream(targetJar));

        Enumeration<? extends ZipEntry> originEntries = origin.entries();
//        int index = 0;
        while (originEntries.hasMoreElements()) {
            ZipEntry originEntry = originEntries.nextElement();
            final String name = originEntry.getName();
            if (name.endsWith(".class") && name.startsWith("com/nineoldandroids/")) {
                continue;
            }

            try {
                byte[] entryBytes = readZipEntry(origin, originEntry);
                if (name.endsWith(".class")) {
//                    System.out.println("[NineOldAndroidsRewriter] #" + (index++) + ", process " + name);
                    entryBytes = processClass(entryBytes, simpleRemapper);
//                    saveToFile(name, entryBytes);
                }

                targetZipOut.putNextEntry(new ZipEntry(name));
                targetZipOut.write(entryBytes);
                targetZipOut.closeEntry();
            } catch (IOException e) {
                System.out.println("[NineOldAndroidsRewriter] fail processJar jar=" + originJar + ", class=" + name);
                e.printStackTrace();
            }
        }

        targetZipOut.flush();
        targetZipOut.close();
        origin.close();
    }

    public static Map<String, String> generateClassMapper(File originJar) throws IOException {
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

        System.out.println("[NineOldAndroidsRewriter] generateClassMapper is " + classMapper);
        return classMapper;
    }

    private static String convertEntryClassNameToDescriptor(String entryClassName) {
        int classSuffixIndex = entryClassName.length() - ".class".length();
        return entryClassName.substring(0, classSuffixIndex);
//        return "L" + entryClassName.substring(0, classSuffixIndex) + ";";
    }

    private static byte[] readZipEntry(ZipFile zipFile, ZipEntry zipEntry) throws IOException {
        byte[] content = new byte[(int) zipEntry.getSize()];
        int count = 0;
        int len = -1;
        InputStream inputStream = zipFile.getInputStream(zipEntry);
        while ((len = inputStream.read(content, count, content.length - count)) != -1) {
            count += len;

            if (count == content.length) {
                break;
            }
        }
        inputStream.close();

        if (count != content.length) {
            throw new IOException("fail readZipEntry; cannot read name=" + zipEntry.getName() + " fully content(" + content.length + "), read content count is " + count);
        }

        return content;
    }

    private static byte[] processClass(byte[] originClassBytes, Remapper remapper) {
        ClassReader classReader = new ClassReader(originClassBytes);
//        ClassNode classNode = new ClassNode();
//        classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
//        classReader.accept(classNode, 0);

//        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassWriter classWriter = new ClassWriter(0);
        ClassRemapper classRemapper = new ClassRemapper(classWriter, remapper);
//        classNode.accept(classRemapper);
        classReader.accept(classRemapper, 0);
        return classWriter.toByteArray();
    }
}