package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.protocol.ApiKey;
import com.github.nginate.kafka.protocol.ApiKeys;
import com.github.nginate.kafka.protocol.messages.Response;
import com.github.nginate.kafka.protocol.types.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.github.nginate.kafka.protocol.types.TypeName.*;

@Data
@ApiKey(ApiKeys.OFFSET_FETCH)
@EqualsAndHashCode(callSuper = true)
public class OffsetFetchResponse extends Response {
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
