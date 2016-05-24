package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.serialization.ApiKey;
import com.github.nginate.kafka.protocol.KafkaApiKeys;
import com.github.nginate.kafka.serialization.Type;
import lombok.Data;

import static com.github.nginate.kafka.serialization.TypeName.*;

@Data
@ApiKey(KafkaApiKeys.OFFSET_FETCH)
public class OffsetFetchResponse {
    @Type(value = WRAPPER, order = 2)
    private OffsetFetchResponseTopicData[] topicData;

    @Data
    public static class OffsetFetchResponseTopicData {
        @Type(STRING)
        private String topic;
        @Type(value = WRAPPER, order = 1)
        private OffsetFetchResponsePartitionData[] partitionData;

        @Data
        public static class OffsetFetchResponsePartitionData {
            @Type(INT32)
            private Integer partition;
            @Type(value = INT64, order = 1)
            private Long offset;
            @Type(value = STRING, order = 2)
            private String metadata;
            @Type(value = INT16, order = 3)
            private Short errorCode;
        }
    }
}
