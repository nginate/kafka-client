package com.github.nginate.kafka.protocol;

import lombok.Getter;

@Getter
public enum ApiKeys {
    /**
     * The produce API is used to send message sets to the server. For efficiency it allows sending message sets
     * intended for many topic partitions in a single request. The produce API uses the generic message set format,
     * but since no offset has been assigned to the messages at the time of the send the producer is free to fill in
     * that field in any way it likes.
     */
    PRODUCE((short)0),
    /**
     * The fetch API is used to fetch a chunk of one or more logs for some topic-partitions. Logically one specifies the
     * topics, partitions, and starting offset at which to begin the fetch and gets back a chunk of messages. In general,
     * the return messages will have offsets larger than or equal to the starting offset. However, with compressed
     * messages, it's possible for the returned messages to have offsets smaller than the starting offset. The number of
     * such messages is typically small and the caller is responsible for filtering out those messages.
     * Fetch requests follow a long poll model so they can be made to block for a period of time if sufficient data is
     * not immediately available.
     * As an optimization the server is allowed to return a partial message at the end of the message set. Clients
     * should handle this case. One thing to note is that the fetch API requires specifying the partition to consume
     * from. The question is how should a consumer know what partitions to consume from? In particular how can you
     * balance the partitions over a set of consumers acting as a group so that each consumer gets a subset of
     * partitions. We have done this assignment dynamically using zookeeper for the scala and java client. The downside
     * of this approach is that it requires a fairly fat client and a zookeeper connection. We haven't yet created a
     * Kafka API to allow this functionality to be moved to the server side and accessed more conveniently. A simple
     * consumer client can be implemented by simply requiring that the partitions be specified in config, though this
     * will not allow dynamic reassignment of partitions should that consumer fail. We hope to address this gap in the
     * next major release.
     */
    FETCH((short)1),
    /**
     * This API describes the valid offset range available for a set of topic-partitions. As with the produce and fetch
     * APIs requests must be directed to the broker that is currently the leader for the partitions in question. This
     * can be determined using the metadata API.
     * The response contains the starting offset of each segment for the requested partition as well as the
     * "log end offset" i.e. the offset of the next message that would be appended to the given partition. We agree that
     * this API is slightly funky.
     */
    LIST_OFFSETS((short)2),
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
    METADATA((short)3),
    LEADER_AND_ISR((short)4),
    STOP_REPLICA((short)5),
    UPDATE_METADATA((short)6),
    /**
     * Note that controlled shutdown will only succeed if all the partitions hosted on the broker have replicas
     * (i.e. the replication factor is greater than 1 and at least one of these replicas is alive). This is generally
     * what you want since shutting down the last replica would make that topic partition unavailable.
     */
    CONTROLLED_SHUTDOWN((short) 7),
    /**
     * This api saves out the consumer's position in the stream for one or more partitions. In the scala API this
     * happens when the consumer calls commit() or in the background if "autocommit" is enabled. This is the position
     * the consumer will pick up from if it crashes before its next commit().
     */
    OFFSET_COMMIT((short)8),
    /**
     * This api reads back a consumer position previously written using the OffsetCommit api. Note that if there is no
     * offset associated with a topic-partition under that consumer group the broker does not set an error code (since
     * it is not really an error), but returns empty metadata and sets the offset field to -1.
     */
    OFFSET_FETCH((short)9),
    GROUP_COORDINATOR((short)10), //FIXME CONSUMER_METADATA in server
    /**
     * The purpose of the initial phase is to set the active members of the group. This protocol has similar semantics
     * as in the initial consumer rewrite design. After finding the coordinator for the group, each member sends a
     * JoinGroup request containing member-specific metadata. The join group request will park at the coordinator until
     * all expected members have sent their own join group requests ("expected" in this case means all members that were
     * part of the previous generation). Once they have done so, the coordinator randomly selects a leader from the
     * group and sends JoinGroup responses to all the pending requests.
     * The JoinGroup request contains an array with the group protocols that it supports along with member-specific
     * metadata. This is basically used to ensure compatibility of group member metadata within the group. The
     * coordinator chooses a protocol which is supported by all members of the group and returns it in the respective
     * JoinGroup responses. If a member joins and doesn't support any of the protocols used by the rest of the group,
     * then it will be rejected. This mechanism provides a way to update protocol metadata to a new format in a rolling
     * upgrade scenario. The newer version will provide metadata for the new protocol and for the old protocol, and the
     * coordinator will choose the old protocol until all members have been upgraded.
     */
    JOIN_GROUP((short)11),
    /**
     * Once a member has joined and synced, it will begin sending periodic heartbeats to keep itself in the group. If
     * not heartbeat has been received by the coordinator with the configured session timeout, the member will be kicked
     * out of the group.
     */
    HEARTBEAT((short)12),
    LEAVE_GROUP((short)13),
    SYNC_GROUP((short)14),
    DESCRIBE_GROUPS((short)15),
    LIST_GROUPS((short)16);

    private final short id;

    ApiKeys(short id) {
        this.id = id;
    }
}
