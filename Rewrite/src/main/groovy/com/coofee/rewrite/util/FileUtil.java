package com.coofee.rewrite.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.gradle.internal.util.BiFunction;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class FileUtil {

    public static URLClassLoader from(File... files) throws MalformedURLException {
        if (files == null || files.length < 1) {
            return null;
        }

        URL[] urls = new URL[files.length];
        for (int i = 0; i < files.length; i++) {
            urls[i] = files[i].toURI().toURL();
        }

        return new URLClassLoader(urls);
    }

    public static void eachFileRecurse(File file, Consumer<File> fileConsumer) {
        if (file == null || !file.exists()) {
            return;
        }

        if (file.isFile()) {
            fileConsumer.accept(file);
            return;
        }

        File[] files = file.listFiles();
        if (files == null) {
            return;
        }

        for (File f : files) {
            if (f.isDirectory()) {
                eachFileRecurse(f, fileConsumer);
            } else {
                fileConsumer.accept(f);
            }
        }
    }

    public static String sha1(File file) {
        InputStream inputStream = null;

        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] buf = new byte[16384];
            inputStream = new FileInputStream(file);

            for (int read = inputStream.read(buf); read != -1; read = inputStream.read(buf)) {
                sha1.update(buf, 0, read);
            }

            return StringUtil.hex(sha1.digest());
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException var14) {
                }
            }

        }

        return null;
    }


    public static void traverseJarClass(File inputJar, File outputJar, BiFunction<byte[], String, byte[]> bytecodeTransform) throws Throwable {
        if (outputJar.exists()) {
            outputJar.delete();
        }

        ZipFile zipFileInput = null;
        ZipOutputStream zipOutput = null;

        try {
            zipFileInput = new ZipFile(inputJar);
            zipOutput = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outputJar)));
            Enumeration<? extends ZipEntry> originEntries = zipFileInput.entries();
            while (originEntries.hasMoreElements()) {
                ZipEntry originEntry = originEntries.nextElement();
                final String name = originEntry.getName();
                byte[] bytecode = new byte[(int) originEntry.getSize()];
                IOUtils.readFully(zipFileInput.getInputStream(originEntry), bytecode);
                if (name.endsWith(".class") && ClassUtil.isValidClassBytes(bytecode)) {
                    bytecode = bytecodeTransform.apply(name, bytecode);
                }
                zipOutput.putNextEntry(new ZipEntry(name));
                zipOutput.write(bytecode);
                zipOutput.closeEntry();
            }
        } catch (Throwable e) {
            System.out.println("[RewritePlugin] fail processJar jar=" + inputJar);
            throw e;
        } finally {
            IOUtils.closeQuietly(zipFileInput);
            IOUtils.closeQuietly(zipOutput);
        }
    }

    public static void traverseJarClass(File inputJar, Consumer<byte[]> byteCodeConsumer) throws Throwable {
        ZipFile zipFileInput = null;

        try {
            zipFileInput = new ZipFile(inputJar);
            Enumeration<? extends ZipEntry> originEntries = zipFileInput.entries();
            while (originEntries.hasMoreElements()) {
                ZipEntry originEntry = originEntries.nextElement();
                final String name = originEntry.getName();
                byte[] bytecode = new byte[(int) originEntry.getSize()];
                IOUtils.readFully(zipFileInput.getInputStream(originEntry), bytecode);
                if (name.endsWith(".class") && ClassUtil.isValidClassBytes(bytecode)) {
                    byteCodeConsumer.accept(bytecode);
                }
            }
        } catch (Throwable e) {
            System.out.println("[RewritePlugin] fail processJar jar=" + inputJar);
            throw e;
        } finally {
            IOUtils.closeQuietly(zipFileInput);
        }
    }
}
