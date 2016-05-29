package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.protocol.KafkaApiKeys;
import com.github.nginate.kafka.serialization.ApiKey;
import com.github.nginate.kafka.serialization.ApiVersion;
import com.github.nginate.kafka.serialization.Type;
import lombok.Data;

import static com.github.nginate.kafka.serialization.TypeName.*;

@Data
@ApiKey(KafkaApiKeys.GROUP_COORDINATOR)
@ApiVersion(0)
public class GroupCoordinatorResponse {
    @Type(value = INT16, order = 2)
    private Short errorCode;
    /**
     * Host and port information for the coordinator for a consumer group.
     */
    @Type(value = WRAPPER, order = 3)
    private GroupCoordinatorBroker coordinator;

    @Data
    public static class GroupCoordinatorBroker {
        /**
         * The broker id.
         */
        @Type(INT32)
        private Integer nodeId;
        /**
         * The hostname of the broker.
         */
        @Type(value = STRING, order = 1)
        private String host;
        /**
         * The port on which the broker accepts requests.
         */
        @Type(value = INT32, order = 2)
        private Integer port;
    }
}
