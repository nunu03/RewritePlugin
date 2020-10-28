package com.coofee.rewrite.reflect;

import com.coofee.rewrite.util.AsmUtil;

import org.apache.commons.io.IOUtils;
import org.gradle.internal.impldep.org.codehaus.plexus.util.FileUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ReflectTest {
    public static void main(String[] args) throws IOException {
        Map<String, Set<String>> classAndMethods = new HashMap<>();
        classAndMethods.put("java/lang/Class", new HashSet<>(Arrays.asList("forName")));
        classAndMethods.put("java/lang/ClassLoader", new HashSet<>(Arrays.asList("loadClass")));
        final ReflectCollector reflectCollector = new ReflectCollector(classAndMethods);

        String classPath = "/" + ReflectTest.class.getName().replace(".", "/") + ".class";
        byte[] classBytes = IOUtils.toByteArray(ReflectTest.class.getResourceAsStream(classPath));
        reflectCollector.transform("", AsmUtil.convert(classBytes));

        final ReflectResult result = ReflectResult.generate(new HashSet<>(), reflectCollector.reflectInfoList);
        String json = result.toJson();
        System.out.println(json);
        FileUtils.fileWrite("reflectTest.json", result.toJson());

        if (result.allReflectExactCount != 7) {
            System.out.println("FAIL.");
        }
    }

    private static void testClassForName() throws ClassNotFoundException, NoSuchMethodException {
        String className = "com.coofee.ReflectTest";

        String name = "other";

        Class.forName("com.coofee.ReflectTest");

        Class.forName(className);

        Class newLineName = Class
                .forName("com.coofee.ReflectTest");
        System.out.println(newLineName);

        Class.forName("com.coofee.ReflectTest", true, ReflectTest.class.getClassLoader());

        Class.forName(className, true, ReflectTest.class.getClassLoader());

        final Method paramsForName = ReflectResult.class.getDeclaredMethod("with", ReflectTest.class,
                Class.forName("com.coofee.ReflectTest"));
        System.out.println(paramsForName);
    }

    private static void testClassLoader() throws ClassNotFoundException {
        ClassLoader classLoader = ReflectResult.class.getClassLoader();
        Class hwNotchSizeUtil = classLoader.loadClass("com.coofee.ReflectTest");
        System.out.println(hwNotchSizeUtil);
    }

    private static void with(String p0, Class<?> p1, Class<?> targetClass) {
        System.out.println("with: p0=" + p0 + ", p1=" + p1 + ", targetClass=" + targetClass);
    }
}
