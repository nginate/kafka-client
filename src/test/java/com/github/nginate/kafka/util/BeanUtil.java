package com.github.nginate.kafka.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.dockerjava.api.command.DockerCmd;
import com.google.common.base.Throwables;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.util.List;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

@UtilityClass
public final class BeanUtil {
    public static <T extends DockerCmd> void copyDockerDto(T source, T dest) {
        List<Field> dtoDataFields = stream(source.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(JsonProperty.class)).collect(toList());
        dtoDataFields.forEach(field -> {
            try {
                field.setAccessible(true);
                Object sourceValue = field.get(source);
                field.set(dest, sourceValue);
            } catch (IllegalAccessException e) {
                throw Throwables.propagate(e);
            }
        });
    }
}
