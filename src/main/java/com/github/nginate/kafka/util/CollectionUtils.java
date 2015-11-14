package com.github.nginate.kafka.util;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.unmodifiableList;

@UtilityClass
public final class CollectionUtils {
    public static <E> Optional<List<E>> unmodifiedOptionalCopy(List<E> original) {
        return original != null ? Optional.of(unmodifiableList(original)) : Optional.empty();
    }
}
