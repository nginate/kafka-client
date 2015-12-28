package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.protocol.ApiKey;
import com.github.nginate.kafka.protocol.ApiKeys;
import com.github.nginate.kafka.protocol.messages.Response;
import com.github.nginate.kafka.protocol.types.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.github.nginate.kafka.protocol.types.TypeName.*;

@Data
@ApiKey(ApiKeys.LIST_OFFSETS)
@EqualsAndHashCode(callSuper = true)
public class OffsetResponse extends Response {
    @Type(value = WRAPPER, order = 2)
    private OffsetResponseTopicData[] topicData;

    @Data
    public static class OffsetResponseTopicData {
        @Type(STRING)
        private String topic;
        @Type(value = WRAPPER, order = 1)
        private PartitionOffsets partitionOffsets;

        @Data
        public static class PartitionOffsets {
            @Type(INT32)
            private Integer partition;
            @Type(value = INT16, order = 1)
            private Short errorCode;
            @Type(value = INT64, order = 2)
            private Long[] offsets;
        }
    }
}
