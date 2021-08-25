package com.coofee.rewrite.manifest;

class Util {

    static String fullClassName(String packageName, String className) {
        if (className.startsWith('.')) {
            return packageName + className
        }

        return className
    }

    static boolean parseBoolean(String value) {
        if (value == null) {
            return false;
        }

        if (value instanceof String) {
            try {
                return Boolean.parseBoolean(value)
            } catch (Throwable e) {
                e.printStackTrace()
            }
        }

        return false
    }
}