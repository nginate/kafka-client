package com.github.nginate.kafka.protocol.messages.request;

import com.github.nginate.kafka.serialization.ApiKey;
import com.github.nginate.kafka.protocol.KafkaApiKeys;
import com.github.nginate.kafka.protocol.messages.dto.Broker;
import com.github.nginate.kafka.protocol.messages.dto.PartitionsStateInfoWrapper;
import com.github.nginate.kafka.serialization.Type;
import lombok.Builder;
import lombok.Data;

import static com.github.nginate.kafka.serialization.TypeName.INT32;
import static com.github.nginate.kafka.serialization.TypeName.WRAPPER;

@Data
@Builder
@ApiKey(KafkaApiKeys.LEADER_AND_ISR)
public class LeaderAndIsrRequest {
    @Type(value = INT32, order = 4)
    private Integer controllerId;
    @Type(value = INT32, order = 5)
    private Integer controllerEpoch;
    @Type(value = WRAPPER, order = 6)
    private PartitionsStateInfoWrapper[] partitionStateInfoWrappers;
    @Type(value = WRAPPER, order = 7)
    private Broker[] leaders;

}
