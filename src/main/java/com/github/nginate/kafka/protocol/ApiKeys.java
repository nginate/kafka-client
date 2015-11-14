package com.github.nginate.kafka.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApiKeys {
    PRODUCE(0, "produce"),
    FETCH(1, "fetch"),
    LIST_OFFSETS(2, "list_offsets"),
    METADATA(3, "metadata"),
    LEADER_AND_ISR(4, "leader_and_isr"),
    STOP_REPLICA(5, "stop_replica"),
    OFFSET_COMMIT(8, "offset_commit"),
    OFFSET_FETCH(9, "offset_fetch"),
    CONSUMER_METADATA(10, "consumer_metadata"),
    JOIN_GROUP(11, "join_group"),
    HEARTBEAT(12, "heartbeat");

    /** the perminant and immutable id of an API--this can't change ever */
    private final int id;
    /** an english description of the api--this is for debugging and can change */
    private final String name;

}
