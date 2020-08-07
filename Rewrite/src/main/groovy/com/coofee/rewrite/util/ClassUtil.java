package com.coofee.rewrite.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class ClassUtil {
    private static final byte[] MAGIC_CAFEBABE = new byte[]{-54, -2, -70, -66};

    public static boolean isValidClassBytes(byte[] classBytecode) {
        if (classBytecode.length < MAGIC_CAFEBABE.length) {
            return false;
        }

        return (
                MAGIC_CAFEBABE[0] == classBytecode[0]
                        && MAGIC_CAFEBABE[1] == classBytecode[1]
                        && MAGIC_CAFEBABE[2] == classBytecode[2]
                        && MAGIC_CAFEBABE[3] == classBytecode[3]
        );
    }

    public static byte[] getClassBytes(Class<?> clazz) throws IOException {
        String classPath = "/" + clazz.getName().replace('.', '/') + ".class";
        final InputStream classInput = clazz.getClass().getResourceAsStream(classPath);
        final byte[] bytes = IOUtils.toByteArray(classInput);
        return bytes;
    }
}
