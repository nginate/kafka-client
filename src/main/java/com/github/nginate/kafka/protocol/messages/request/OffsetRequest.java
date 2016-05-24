package com.github.nginate.kafka.protocol.messages.request;

import com.github.nginate.kafka.serialization.ApiKey;
import com.github.nginate.kafka.protocol.KafkaApiKeys;
import com.github.nginate.kafka.serialization.Type;
import lombok.Builder;
import lombok.Data;

import static com.github.nginate.kafka.serialization.TypeName.*;

/**
 * This API describes the valid offset range available for a set of topic-partitions. As with the produce and fetch
 * APIs requests must be directed to the broker that is currently the leader for the partitions in question. This
 * can be determined using the metadata API.
 * The response contains the starting offset of each segment for the requested partition as well as the
 * "log end offset" i.e. the offset of the next message that would be appended to the given partition. We agree that
 * this API is slightly funky.
 */
@Data
@Builder
@ApiKey(KafkaApiKeys.LIST_OFFSETS)
public class OffsetRequest {
    @Type(value = INT32, order = 4)
    private Integer replicaId;
    @Type(value = WRAPPER, order = 5)
    private OffsetRequestTopicData[] topicData;

    @Data
    public static class OffsetRequestTopicData {
        @Type(STRING)
        private String topic;
        @Type(value = WRAPPER, order = 1)
        private OffsetRequestPartitionData[] partitionData;

        @Data
        public static class OffsetRequestPartitionData {
            @Type(INT32)
            private Integer partition;
            /**
             * Used to ask for all messages before a certain time (ms). There are two special values. Specify -1 to
             * receive the latest offset (i.e. the offset of the next coming message) and -2 to receive the earliest
             * available offset. Note that because offsets are pulled in descending order, asking for the earliest
             * offset will always return you a single element.
             */
            @Type(value = INT64, order = 1)
            private Long time;
            @Type(value = INT32, order = 2)
            private Integer maxNumberOfOffsets;
        }
    }
}
