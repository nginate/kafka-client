package com.github.nginate.kafka.protocol.messages.dto;

import lombok.Value;

@Value
public class OffsetAndMetadata {
    private final long offset;
    private final String metadata;
}
