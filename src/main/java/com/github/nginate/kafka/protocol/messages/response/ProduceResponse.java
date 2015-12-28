package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.protocol.ApiKey;
import com.github.nginate.kafka.protocol.ApiKeys;
import com.github.nginate.kafka.protocol.messages.Response;
import com.github.nginate.kafka.protocol.types.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.github.nginate.kafka.protocol.types.TypeName.*;

@Data
@ApiKey(ApiKeys.PRODUCE)
@EqualsAndHashCode(callSuper = true)
public class ProduceResponse extends Response {
    @Type(value = WRAPPER, order = 2)
    private ProduceResponseData[] produceResponseData;

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
        }
    }
}
