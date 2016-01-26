package com.github.nginate.kafka.network.client;

import lombok.Value;

@Value
public class BinaryMessageMetadata {
    private final Class<?> responseType;
}
