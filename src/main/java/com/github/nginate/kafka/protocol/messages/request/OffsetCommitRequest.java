package com.github.nginate.kafka.protocol.messages.request;

import com.github.nginate.kafka.protocol.KafkaApiKeys;
import com.github.nginate.kafka.serialization.ApiKey;
import com.github.nginate.kafka.serialization.ApiVersion;
import com.github.nginate.kafka.serialization.Type;
import lombok.Builder;
import lombok.Data;

import static com.github.nginate.kafka.serialization.TypeName.*;

/**
 * This api saves out the consumer's position in the stream for one or more partitions. In the scala API this happens
 * when the consumer calls commit() or in the background if "autocommit" is enabled. This is the position the consumer
 * will pick up from if it crashes before its next commit().
 * These fields should be mostly self explanatory, except for metadata. This is meant as a way to attach arbitrary
 * metadata that should be committed with this offset commit. It could be the name of a file that contains state
 * information for the processor, or a small piece of state. Basically it is just a generic string field that will be
 * passed back to the client when the offset is fetched. It will likely have a tight size limit to avoid server impact.
 * <p>
 * In v0 and v1, the time stamp of each partition is defined as the commit time stamp, and the offset coordinator will
 * retain the committed offset until its commit time stamp + offset retention time specified in the broker config; if
 * the time stamp field is not set, brokers will set the commit time as the receive time before committing the offset,
 * users can explicitly set the commit time stamp if they want to retain the committed offset longer on the broker than
 * the configured offset retention time.
 * In v2, we removed the time stamp field but add a global retention time field (see KAFKA-1634 for details); brokers
 * will then always set the commit time stamp as the receive time, but the committed offset can be retained until its
 * commit time stamp + user specified retention time in the commit request. If the retention time is not set, the broker
 * offset retention time will be used as default.
 */
@Data
@Builder
@ApiKey(KafkaApiKeys.OFFSET_COMMIT)
@ApiVersion(2)
public class OffsetCommitRequest {
    @Type(value = STRING, order = 4)
    private String consumerGroupId;
    @Type(value = INT32, order = 5)
    private Integer consumerGroupGenerationId;
    @Type(value = STRING, order = 6)
    private String consumerId;
    @Type(value = INT64, order = 7)
    private Long retentionTime;
    @Type(value = WRAPPER, order = 8)
    private OffsetCommitRequestTopicData[] topicData;

    @Data
    public static class OffsetCommitRequestTopicData {
        @Type(STRING)
        private String topic;
        @Type(value = WRAPPER, order = 1)
        private OffsetCommitRequestPartitionData[] partitionData;

        @Data
        public static class OffsetCommitRequestPartitionData {
            @Type(INT32)
            private Integer partition;
            @Type(value = INT64, order = 1)
            private Long offset;
            @Type(value = INT64, order = 2)
            private Long timestamp;
            @Type(value = STRING, order = 3)
            private String metadata;
        }
    }
}
