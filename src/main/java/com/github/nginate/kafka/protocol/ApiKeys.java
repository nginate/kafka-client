package com.github.nginate.kafka.protocol;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum ApiKeys {
    /**
     * The produce API is used to send message sets to the server. For efficiency it allows sending message sets
     * intended for many topic partitions in a single request. The produce API uses the generic message set format,
     * but since no offset has been assigned to the messages at the time of the send the producer is free to fill in
     * that field in any way it likes.
     */
    PRODUCE(0),
    FETCH(1),
    LIST_OFFSETS(2),
    /**
     * This API answers the following questions:
     *  What topics exist?
     *  How many partitions does each topic have?
     *  Which broker is currently the leader for each partition?
     *  What is the host and port for each of these brokers?
     * This is the only request that can be addressed to any broker in the cluster. Since there may be many topics the
     * client can give an optional list of topic names in order to only return metadata for a subset of topics. The
     * metadata returned is at the partition level, but grouped together by topic for convenience and to avoid
     * redundancy. For each partition the metadata contains the information for the leader as well as for all the
     * replicas and the list of replicas that are currently in-sync. Note: If "auto.create.topics.enable" is set in the
     * broker configuration, a topic metadata request will create the topic with the default replication factor and
     * number of partitions.
     */
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
