package com.github.nginate.kafka.protocol.messages.request;

import com.github.nginate.kafka.protocol.KafkaApiKeys;
import com.github.nginate.kafka.protocol.messages.dto.PartitionStateInfoWrapper;
import com.github.nginate.kafka.serialization.ApiKey;
import com.github.nginate.kafka.serialization.ApiVersion;
import com.github.nginate.kafka.serialization.Type;
import lombok.Builder;
import lombok.Data;

import static com.github.nginate.kafka.serialization.TypeName.*;

@Data
@Builder
@ApiKey(KafkaApiKeys.UPDATE_METADATA)
@ApiVersion(2)
public class UpdateMetadataRequest {
    @Type(INT32)
    private Integer controllerId;
    @Type(value = INT32, order = 1)
    private Integer controllerEpoch;
    @Type(value = WRAPPER, order = 2)
    private PartitionStateInfoWrapper[] partitionStates;
    @Type(value = WRAPPER, order = 3)
    private UpdateMetadataBroker[] liveBrokers;

    @Data
    public static class UpdateMetadataBroker {
        @Type(INT32)
        private Integer nodeId;
        @Type(value = WRAPPER, order = 1)
        private UpdateMetadataEndPoint[] endPoints;
        @Type(value = STRING, order = 2)
        private String rack;
    }

    @Data
    public static class UpdateMetadataEndPoint {
        /**
         * The port on which the broker accepts requests.
         */
        @Type(INT32)
        private Integer port;
        /**
         * The hostname of the broker.
         */
        @Type(value = STRING, order = 1)
        private String host;
        /**
         * The security protocol type.
         */
        @Type(value = INT16, order = 2)
        private Short securityProtocolType;
    }
}
