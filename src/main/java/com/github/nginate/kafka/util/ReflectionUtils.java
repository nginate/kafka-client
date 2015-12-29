package com.github.nginate.kafka.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.util.Arrays.stream;

@UtilityClass
public final class ReflectionUtils {

    public static void doWithFields(Class<?> clazz, Predicate<Field> filter, Consumer<Field> callback) {
        stream(FieldUtils.getAllFields(clazz)).filter(filter).forEach(field -> {
            field.setAccessible(true);
            callback.accept(field);
        });
    }

    public static void doWithSortedFields(Class<?> clazz, Predicate<Field> filter, Comparator<? super Field> comparator,
                                          Consumer<Field> callback) {
        stream(FieldUtils.getAllFields(clazz)).filter(filter).sorted(comparator).forEachOrdered(field -> {
            field.setAccessible(true);
            callback.accept(field);
        });
    }
}
