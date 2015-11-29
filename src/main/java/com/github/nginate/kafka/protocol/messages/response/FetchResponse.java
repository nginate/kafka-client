package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.protocol.ApiKey;
import com.github.nginate.kafka.protocol.ApiKeys;
import com.github.nginate.kafka.protocol.messages.MessageSet;
import com.github.nginate.kafka.protocol.messages.Response;
import com.github.nginate.kafka.protocol.types.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.github.nginate.kafka.protocol.types.TypeName.*;

@Data
@ApiKey(ApiKeys.FETCH)
@EqualsAndHashCode(callSuper = true)
public class FetchResponse extends Response {
    @Type(WRAPPER)
    private FetchResponseTopicData[] topicData;

    @Data
    public static class FetchResponseTopicData {
        /**
         * The name of the topic this response entry is for.
         */
        @Type(STRING)
        private String topic;
        @Type(WRAPPER)
        private FetchResponsePartitionData[] partitionData;

        @Data
        public static class FetchResponsePartitionData {
            /**
             * The id of the partition this response is for.
             */
            @Type(INT32)
            private Integer partition;
            @Type(INT16)
            private Short errorCode;
            /**
             * The offset at the end of the log for this partition. This can be used by the client to determine how many
             * messages behind the end of the log they are.
             */
            @Type(INT64)
            private Long highWatermarkOffset;
            /**
             * The size in bytes of the message set for this partition
             */
            @Type(INT32)
            private Integer messageSetSize;
            /**
             * The message data fetched from this partition, in the format described above.
             */
            @Type(WRAPPER)
            private MessageSet messageSet;
        }
    }
}
