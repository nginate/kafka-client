package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.protocol.ApiKey;
import com.github.nginate.kafka.protocol.ApiKeys;
import com.github.nginate.kafka.protocol.types.Type;
import lombok.Data;

import static com.github.nginate.kafka.protocol.types.TypeName.*;

/**
 * The response contains metadata for each partition, with partitions grouped together by topic. This metadata refers to
 * brokers by their broker id. The brokers each have a host and port.
 */
@Data
@ApiKey(ApiKeys.METADATA)
public class MetadataResponse {
    @Type(value = WRAPPER, order = 2)
    private Broker[] brokers;
    @Type(value = WRAPPER, order = 3)
    private TopicMetadata[] topicMetadata;

    @Data
    public static class Broker {
        @Type(INT32)
        private Integer nodeId;
        @Type(value = STRING, order = 1)
        private String host;
        @Type(value = INT32, order = 2)
        private Integer port;
    }

    @Data
    public static class TopicMetadata {
        @Type(INT16)
        private Short topicErrorCode;
        @Type(value = STRING, order = 1)
        private String topicName;
        @Type(value = WRAPPER, order = 2)
        private PartitionMetadata[] partitionMetadata;

        @Data
        public static class PartitionMetadata {
            @Type(INT16)
            private Short partitionErrorCode;
            @Type(value = INT32, order = 1)
            private Integer partitionId;
            /**
             * The node id for the kafka broker currently acting as leader for this partition. If no leader exists
             * because we are in the middle of a leader election this id will be -1.
             */
            @Type(value = INT32, order = 2)
            private Integer leader;
            /**
             * The set of alive nodes that currently acts as slaves for the leader for this partition.
             */
            @Type(value = INT32, order = 3)
            private Integer[] replicas;
            /**
             * The set subset of the replicas that are "caught up" to the leader
             */
            @Type(value = INT32, order = 4)
            private Integer[] isr;
        }
    }
}
