package com.github.nginate.kafka.network.client;

import com.google.common.collect.Maps;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Map;
import java.util.Optional;

@ThreadSafe
public class BinaryClientContext {
    private final Map<Object, BinaryMessageMetadata> responseTypeMap = Maps.newConcurrentMap();

    public Optional<BinaryMessageMetadata> getMetadata(Object correlationId) {
        return Optional.ofNullable(responseTypeMap.get(correlationId));
    }

    public Optional<BinaryMessageMetadata> removeMetadata(Object correlationId) {
        return Optional.ofNullable(responseTypeMap.remove(correlationId));
    }

    public void addMetadata(Object correlationId, BinaryMessageMetadata metadata) {
        responseTypeMap.put(correlationId, metadata);
    }

    public void clear() {
        responseTypeMap.clear();
    }
}
