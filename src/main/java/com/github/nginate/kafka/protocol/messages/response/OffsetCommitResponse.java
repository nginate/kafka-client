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
    @Type(WRAPPER)
    private OffsetCommitResponseTopicData[] topicData;

    @Data
    public static class OffsetCommitResponseTopicData {
        @Type(STRING)
        private String topic;
        @Type(WRAPPER)
        private OffsetCommitResponsePartitionData[] partitionData;

        @Data
        public static class OffsetCommitResponsePartitionData {
            @Type(INT32)
            private Integer partition;
            @Type(INT16)
            private Short errorCode;
        }
    }
}
