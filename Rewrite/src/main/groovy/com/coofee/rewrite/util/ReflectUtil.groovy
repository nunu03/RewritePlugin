package com.coofee.rewrite.util

import java.lang.reflect.Field

class ReflectUtil {

    public static <T> T getFieldValue(Object object, String propertyName) {
        Field field = object.class.getDeclaredField(propertyName)
        field.setAccessible(true);
        return (T) field.get(object);
    }

    public static <T> T getExtension(Object object, String extensionName) {
        return object?."${extensionName}"
    }
}