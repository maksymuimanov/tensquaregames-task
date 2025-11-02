package io.maksymuimanov.task.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class ReflectionUtils {
    private ReflectionUtils() {
    }

    public static void callMethod(Object object, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Throwable {
        try {
            Class<?> clazz = object.getClass();
            Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            method.invoke(object, parameters);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
