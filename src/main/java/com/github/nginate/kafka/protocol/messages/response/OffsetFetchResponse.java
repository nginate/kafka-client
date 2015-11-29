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
    @Type(WRAPPER)
    private OffsetFetchResponseTopicData[] topicData;

    @Data
    public static class OffsetFetchResponseTopicData{
        @Type(STRING)
        private String topic;
        @Type(WRAPPER)
        private OffsetFetchResponsePartitionData[] partitionData;

        @Data
        public static class OffsetFetchResponsePartitionData {
            @Type(INT32)
            private Integer partition;
            @Type(INT64)
            private Long offset;
            @Type(STRING)
            private String metadata;
            @Type(INT16)
            private Short errorCode;
        }
    }
}
