package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.protocol.KafkaApiKeys;
import com.github.nginate.kafka.protocol.messages.MessageSet;
import com.github.nginate.kafka.serialization.ApiKey;
import com.github.nginate.kafka.serialization.ApiVersion;
import com.github.nginate.kafka.serialization.Type;
import lombok.Data;

import static com.github.nginate.kafka.serialization.TypeName.*;

@Data
@ApiKey(KafkaApiKeys.FETCH)
@ApiVersion(2)
public class FetchResponse {
    @Type(value = WRAPPER, order = 2)
    private FetchResponseTopicData[] topicData;

    @Data
    public static class FetchResponseTopicData {
        /**
         * The name of the topic this response entry is for.
         */
        @Type(STRING)
        private String topic;
        @Type(value = WRAPPER, order = 1)
        private FetchResponsePartitionData[] partitionData;

        @Data
        public static class FetchResponsePartitionData {
            /**
             * The id of the partition this response is for.
             */
            @Type(INT32)
            private Integer partition;
            @Type(value = INT16, order = 1)
            private Short errorCode;
            /**
             * The offset at the end of the log for this partition. This can be used by the client to determine how many
             * messages behind the end of the log they are.
             */
            @Type(value = INT64, order = 2)
            private Long highWatermarkOffset;
            /**
             * The size in bytes of the message set for this partition
             */
            @Type(value = INT32, order = 3)
            private Integer messageSetSize;
            /**
             * The message data fetched from this partition, in the format described above.
             */
            @Type(value = WRAPPER, order = 4)
            private MessageSet messageSet;
        }
    }
}
