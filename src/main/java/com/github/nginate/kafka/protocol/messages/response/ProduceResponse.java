package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.protocol.KafkaApiKeys;
import com.github.nginate.kafka.serialization.ApiKey;
import com.github.nginate.kafka.serialization.ApiVersion;
import com.github.nginate.kafka.serialization.Type;
import lombok.Data;

import static com.github.nginate.kafka.serialization.TypeName.*;

@Data
@ApiKey(KafkaApiKeys.PRODUCE)
@ApiVersion(2)
public class ProduceResponse {
    @Type(value = WRAPPER, order = 2)
    private ProduceResponseData[] produceResponseData;
    /**
     * Duration in milliseconds for which the request was throttled due to quota violation.
     * (Zero if the request did not violate any quota.)
     */
    @Type(value = INT32, order = 3)
    private Integer throttleTimeMs;

    @Data
    public static class ProduceResponseData {
        /**
         * The topic this response entry corresponds to.
         */
        @Type(STRING)
        private String topic;
        @Type(value = WRAPPER, order = 1)
        private ProduceResponsePartitionData[] produceResponsePartitionData;

        @Data
        public static class ProduceResponsePartitionData {
            /**
             * The partition this response entry corresponds to.
             */
            @Type(INT32)
            private Integer partition;
            /**
             * The error from this partition, if any. Errors are given on a per-partition basis because a given
             * partition may be unavailable or maintained on a different host, while others may have successfully
             * accepted the produce request.
             */
            @Type(value = INT16, order = 1)
            private Short errorCode;
            /**
             * The offset assigned to the first message in the message set appended to this partition.
             */
            @Type(value = INT64, order = 2)
            private Long offset;
            /**
             * The timestamp returned by broker after appending the messages.
             * If CreateTime is used for the topic, the timestamp will be -1.
             * If LogAppendTime is used for the topic, the timestamp will be
             * the broker local time when the messages are appended.
             */
            @Type(value = INT64, order = 3)
            private Long timestamp;
        }
    }
}
