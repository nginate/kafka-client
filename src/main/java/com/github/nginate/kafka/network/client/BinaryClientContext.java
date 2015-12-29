package com.github.nginate.kafka.network.client;

import com.google.common.collect.Maps;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Map;
import java.util.Optional;

@ThreadSafe
public class BinaryClientContext {
    private final Map<Object, Class<?>> responseTypeMap = Maps.newConcurrentMap();

    public Optional<Class<?>> getResponseType(Object correlationId) {
        return Optional.ofNullable(responseTypeMap.get(correlationId));
    }

    public Optional<Class<?>> removeMetadata(Object correlationId) {
        return Optional.ofNullable(responseTypeMap.remove(correlationId));
    }

    public void addResponseType(Object correlationId, Class<?> responseClass) {
        responseTypeMap.put(correlationId, responseClass);
    }
}
