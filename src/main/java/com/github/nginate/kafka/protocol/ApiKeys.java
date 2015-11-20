package com.github.nginate.kafka.protocol;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum ApiKeys {
    PRODUCE(0),
    FETCH(1),
    LIST_OFFSETS(2),
    METADATA(3),
    LEADER_AND_ISR(4),
    STOP_REPLICA(5),
    OFFSET_COMMIT(8),
    OFFSET_FETCH(9),
    CONSUMER_METADATA(10),
    JOIN_GROUP(11),
    HEARTBEAT(12);

    private final int id;
}
