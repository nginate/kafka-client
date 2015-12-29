package com.github.nginate.kafka.util;

import lombok.experimental.UtilityClass;

import java.util.*;

@UtilityClass
public final class CollectionUtils {
    public static <E> Optional<List<E>> unmodifiedOptionalCopy(List<E> original) {
        return Optional.ofNullable(original).map(Collections::unmodifiableList);
    }

    public static <E> boolean isBlank(Collection<E> collection) {
        return collection == null || collection.isEmpty();
    }

    public static <K, V> boolean isBlank(Map<K, V> map) {
        return map == null || map.isEmpty();
    }
}
