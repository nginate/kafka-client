package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.protocol.ApiKey;
import com.github.nginate.kafka.protocol.ApiKeys;
import com.github.nginate.kafka.protocol.types.Type;
import com.github.nginate.kafka.protocol.types.TypeName;
import lombok.Data;

import static com.github.nginate.kafka.protocol.types.TypeName.*;

@Data
@ApiKey(ApiKeys.LEADER_AND_ISR)
public class LeaderAndIsrResponse {
    @Type(value = INT16, order = 2)
    private Short errorCode;
    @Type(value = WRAPPER, order = 3)
    private ResponseMap[] responseMaps;

    @Data
    public static class ResponseMap {
        @Type(STRING)
        private String topic;
        @Type(value = INT32, order = 1)
        private Integer partition;
        @Type(value = INT16, order = 2)
        private Short partitionErrorCode;
    }
}
