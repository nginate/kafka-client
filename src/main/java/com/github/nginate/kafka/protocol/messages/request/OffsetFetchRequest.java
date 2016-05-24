package com.github.nginate.kafka.protocol.messages.request;

import com.github.nginate.kafka.serialization.ApiKey;
import com.github.nginate.kafka.protocol.KafkaApiKeys;
import com.github.nginate.kafka.serialization.Type;
import lombok.Builder;
import lombok.Data;

import static com.github.nginate.kafka.serialization.TypeName.*;

/**
 * This api reads back a consumer position previously written using the OffsetCommit api. Note that if there is no
 * offset associated with a topic-partition under that consumer group the broker does not set an error code (since it is
 * not really an error), but returns empty metadata and sets the offset field to -1.
 */
@Data
@Builder
@ApiKey(KafkaApiKeys.OFFSET_FETCH)
public class OffsetFetchRequest {
    @Type(value = STRING, order = 4)
    private String consumerGroup;
    @Type(value = WRAPPER, order = 5)
    private OffsetFetchRequestTopicData[] topicData;

    @Data
    public static class OffsetFetchRequestTopicData {
        @Type(STRING)
        private String topic;
        @Type(value = INT32, order = 1)
        private Integer[] partitions;
    }
}
