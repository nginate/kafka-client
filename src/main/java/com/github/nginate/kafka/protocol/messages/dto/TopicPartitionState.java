package com.github.nginate.kafka.protocol.messages.dto;

import lombok.Data;

@Data
public class TopicPartitionState {
    private Long position; // last consumed position
    private OffsetAndMetadata committed;  // last committed position
    private boolean paused;  // whether this partition has been paused by the user
    private OffsetResetStrategy resetStrategy;  // the strategy to use if the offset needs resetting
}
