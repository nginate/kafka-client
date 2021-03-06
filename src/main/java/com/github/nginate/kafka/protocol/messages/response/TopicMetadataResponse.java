package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.protocol.KafkaApiKeys;
import com.github.nginate.kafka.protocol.messages.dto.Broker;
import com.github.nginate.kafka.serialization.ApiKey;
import com.github.nginate.kafka.serialization.ApiVersion;
import com.github.nginate.kafka.serialization.Type;
import lombok.Data;

import static com.github.nginate.kafka.serialization.TypeName.*;

/**
 * The response contains metadata for each partition, with partitions grouped together by topic. This metadata refers to
 * brokers by their broker id. The brokers each have a host and port.
 */
@Data
@ApiKey(KafkaApiKeys.METADATA)
@ApiVersion(1)
public class TopicMetadataResponse {
    @Type(value = WRAPPER, order = 2)
    private Broker[] brokers;
    @Type(value = INT32, order = 3)
    private Integer controllerId;
    @Type(value = WRAPPER, order = 4)
    private TopicMetadata[] topicMetadata;

    @Data
    public static class TopicMetadata {
        @Type(INT16)
        private Short topicErrorCode;
        @Type(value = STRING, order = 1)
        private String topicName;
        @Type(value = BOOLEAN, order = 2)
        private Boolean internal;
        @Type(value = WRAPPER, order = 3)
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
