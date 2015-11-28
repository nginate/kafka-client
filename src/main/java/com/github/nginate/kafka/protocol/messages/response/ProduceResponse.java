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
    @Type(WRAPPER)
    private ProduceResponseData[] produceResponseData;

    @Data
    public static class ProduceResponseData {
        /**
         * The topic this response entry corresponds to.
         */
        @Type(STRING)
        private String topic;
        @Type(WRAPPER)
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
            @Type(INT16)
            private Short errorCode;
            /**
             * The offset assigned to the first message in the message set appended to this partition.
             */
            @Type(INT64)
            private Long offset;
        }
    }
}
