package com.github.nginate.kafka.protocol.messages.request;

import com.github.nginate.kafka.protocol.ApiKey;
import com.github.nginate.kafka.protocol.ApiKeys;
import com.github.nginate.kafka.protocol.messages.Request;
import com.github.nginate.kafka.protocol.types.Type;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.github.nginate.kafka.protocol.types.TypeName.*;

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
@Data
@Builder
@ApiKey(ApiKeys.FETCH)
@EqualsAndHashCode(callSuper = true)
public class FetchRequest extends Request {
    /**
     * The replica id indicates the node id of the replica initiating this request. Normal client consumers should
     * always specify this as -1 as they have no node id. Other brokers set this to be their own node id. The value -2
     * is accepted to allow a non-broker to issue fetch requests as if it were a replica broker for debugging purposes.
     */
    @Type(INT32)
    private Integer replicaId;
    /**
     * The max wait time is the maximum amount of time in milliseconds to block waiting if insufficient data is
     * available at the time the request is issued.
     */
    @Type(INT32)
    private Integer maxWaitTime;
    /**
     * This is the minimum number of bytes of messages that must be available to give a response. If the client sets
     * this to 0 the server will always respond immediately, however if there is no new data since their last request
     * they will just get back empty message sets. If this is set to 1, the server will respond as soon as at least one
     * partition has at least 1 byte of data or the specified timeout occurs. By setting higher values in combination
     * with the timeout the consumer can tune for throughput and trade a little additional latency for reading only
     * large chunks of data (e.g. setting MaxWaitTime to 100 ms and setting MinBytes to 64k would allow the server to
     * wait up to 100ms to try to accumulate 64k of data before responding).
     */
    @Type(INT32)
    private Integer minBytes;
    @Type(WRAPPER)
    private FetchRequestTopicData[] topicData;

    @Data
    public static class FetchRequestTopicData {
        /**
         * The name of the topic.
         */
        @Type(STRING)
        private String topic;
        @Type(WRAPPER)
        private FetchRequestPartitionData[] partitionData;

        @Data
        public static class FetchRequestPartitionData {
            /**
             * The id of the partition the fetch is for.
             */
            @Type(INT32)
            private Integer partition;
            /**
             * The offset to begin this fetch from.
             */
            @Type(INT64)
            private Long fetchOffset;
            /**
             * The maximum bytes to include in the message set for this partition. This helps bound the size of the
             * response.
             */
            @Type(INT32)
            private Integer maxBytes;
        }
    }
}
