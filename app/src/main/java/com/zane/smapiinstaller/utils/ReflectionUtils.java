package com.zane.smapiinstaller.utils;

import android.os.Build;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class ReflectionUtils {
    public static <T extends Annotation> T getDeclaredAnnotation(Field targetField, Class<T> targetAnnotation) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return targetField.getDeclaredAnnotation(targetAnnotation);
        }
        Annotation[] declaredAnnotations = targetField.getDeclaredAnnotations();
        for (Annotation annotation : declaredAnnotations) {
            if(targetAnnotation.isAssignableFrom(annotation.getClass())) {
                return (T) annotation;
            }
        }
        return null;
    }

    public static <T extends Annotation> T getDeclaredAnnotation(Class<?> targetClass, Class<T> targetAnnotation) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return targetClass.getDeclaredAnnotation(targetAnnotation);
        }
        Annotation[] declaredAnnotations = targetClass.getDeclaredAnnotations();
        for (Annotation annotation : declaredAnnotations) {
            if(targetAnnotation.isAssignableFrom(annotation.getClass())) {
                return (T) annotation;
            }
        }
        return null;
    }
}
