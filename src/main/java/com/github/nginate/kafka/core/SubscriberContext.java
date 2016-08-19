package com.github.nginate.kafka.core;

import com.github.nginate.kafka.protocol.messages.dto.OffsetAndMetadata;
import com.github.nginate.kafka.protocol.messages.dto.OffsetResetStrategy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class SubscriberContext {
    private final Integer minBytes;
    private final Integer maxBytes;
    private final Integer maxWaitMillis;
    /**
     * the strategy to use if the offset needs resetting
     */
    private final OffsetResetStrategy resetStrategy;

    private volatile Long position; // last consumed position
    private OffsetAndMetadata committed;  // last committed position
    private boolean paused;  // whether this partition has been paused by the user

    public void updateOffset(int diff) {
        position += diff;
    }
}
