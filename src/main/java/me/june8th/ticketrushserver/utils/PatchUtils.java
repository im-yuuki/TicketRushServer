package me.june8th.ticketrushserver.utils;

import com.fasterxml.jackson.annotation.JsonView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

public abstract class PatchUtils {

    private static final Logger logger = LoggerFactory.getLogger(PatchUtils.class);

    public static <T> void applyPatch(T target, T patch, Class<?> viewClass) {
        Class<?> clazz = target.getClass();
        int patchedFieldsCount = 0;

        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (!isFieldInView(field, viewClass)) continue;

                field.setAccessible(true);
                try {
                    Object value = field.get(patch);
                    if (value != null) {
                        field.set(target, value);
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to patch field: " + field.getName(), e);
                }
            }
            clazz = clazz.getSuperclass();
        }
        logger.trace("Applied patch with {} fields.", patchedFieldsCount); // Log at trace level
    }

    private static boolean isFieldInView(Field field, Class<?> viewClass) {
        JsonView annotation = field.getAnnotation(JsonView.class);
        if (annotation == null) return false;

        for (Class<?> v : annotation.value()) {
            if (v.isAssignableFrom(viewClass)) return true;
        }
        return false;
    }
}
