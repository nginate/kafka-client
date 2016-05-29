package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.protocol.KafkaApiKeys;
import com.github.nginate.kafka.serialization.ApiKey;
import com.github.nginate.kafka.serialization.ApiVersion;
import com.github.nginate.kafka.serialization.Type;
import lombok.Data;

import static com.github.nginate.kafka.serialization.TypeName.*;

@Data
@ApiKey(KafkaApiKeys.LIST_OFFSETS)
@ApiVersion(0)
public class OffsetResponse {
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
