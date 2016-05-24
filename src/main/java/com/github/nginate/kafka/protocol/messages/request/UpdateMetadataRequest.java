package com.github.nginate.kafka.protocol.messages.request;

import com.github.nginate.kafka.serialization.ApiKey;
import com.github.nginate.kafka.protocol.KafkaApiKeys;
import com.github.nginate.kafka.protocol.messages.dto.Broker;
import com.github.nginate.kafka.protocol.messages.dto.PartitionStateInfoWrapper;
import com.github.nginate.kafka.serialization.Type;
import com.github.nginate.kafka.serialization.TypeName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ApiKey(KafkaApiKeys.UPDATE_METADATA)
public class UpdateMetadataRequest {
    @Type(TypeName.INT32)
    private Integer controllerId;
    @Type(value = TypeName.INT32, order = 1)
    private Integer controllerEpoch;
    @Type(value = TypeName.WRAPPER, order = 2)
    private PartitionStateInfoWrapper[] stateInfoWrappers;
    @Type(value = TypeName.WRAPPER, order = 3)
    private Broker[] aliveBrokers;
}
