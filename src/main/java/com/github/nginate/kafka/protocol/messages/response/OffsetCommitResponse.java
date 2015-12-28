package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.protocol.ApiKey;
import com.github.nginate.kafka.protocol.ApiKeys;
import com.github.nginate.kafka.protocol.messages.Response;
import com.github.nginate.kafka.protocol.types.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.github.nginate.kafka.protocol.types.TypeName.*;

@Data
@ApiKey(ApiKeys.OFFSET_COMMIT)
@EqualsAndHashCode(callSuper = true)
public class OffsetCommitResponse extends Response {
    @Type(value = WRAPPER, order = 2)
    private OffsetCommitResponseTopicData[] topicData;

    @Data
    public static class OffsetCommitResponseTopicData {
        @Type(STRING)
        private String topic;
        @Type(value = WRAPPER, order = 1)
        private OffsetCommitResponsePartitionData[] partitionData;

        @Data
        public static class OffsetCommitResponsePartitionData {
            @Type(INT32)
            private Integer partition;
            @Type(value = INT16, order = 1)
            private Short errorCode;
        }
    }
}
