package com.coofee.rewrite.util

import java.lang.reflect.Field
import java.lang.reflect.Method

class ReflectUtil {

    public static <T> T getFieldValue(Object object, String propertyName) {
        Field field = object.class.getDeclaredField(propertyName)
        field.setAccessible(true);
        return (T) field.get(object);
    }

    public static Object invoke(Object object, String methodName, Class[] parameterTypes, Object[] args) {
        try {
            final Method method = object.class.getDeclaredMethod(methodName, parameterTypes)
            method.setAccessible(true);
            return method.invoke(object, args);
        } catch (Throwable e) {
//            e.printStackTrace()
        }

        return null;
    }

    public static <T> T getExtension(Object object, String extensionName) {
        return object?."${extensionName}"
    }
}