package com.github.nginate.kafka.protocol.messages.request;

import com.github.nginate.kafka.protocol.ApiKey;
import com.github.nginate.kafka.protocol.ApiKeys;
import com.github.nginate.kafka.protocol.messages.Request;
import com.github.nginate.kafka.protocol.types.Type;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.github.nginate.kafka.protocol.types.TypeName.INT32;
import static com.github.nginate.kafka.protocol.types.TypeName.STRING;
import static com.github.nginate.kafka.protocol.types.TypeName.WRAPPER;

/**
 * This api reads back a consumer position previously written using the OffsetCommit api. Note that if there is no
 * offset associated with a topic-partition under that consumer group the broker does not set an error code (since it is
 * not really an error), but returns empty metadata and sets the offset field to -1.
 */
@Data
@Builder
@ApiKey(ApiKeys.OFFSET_FETCH)
@EqualsAndHashCode(callSuper = true)
public class OffsetFetchRequest extends Request {
    @Type(STRING)
    private String consumerGroup;
    @Type(WRAPPER)
    private OffsetFetchRequestTopicData[] topicData;

    @Data
    public static class OffsetFetchRequestTopicData {
        @Type(STRING)
        private String topic;
        @Type(INT32)
        private Integer[] partitions;
    }
}
