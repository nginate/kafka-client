package com.github.nginate.kafka.protocol.messages.request;

import com.github.nginate.kafka.protocol.ApiKey;
import com.github.nginate.kafka.protocol.ApiKeys;
import com.github.nginate.kafka.protocol.messages.dto.Broker;
import com.github.nginate.kafka.protocol.messages.dto.PartitionsStateInfoWrapper;
import com.github.nginate.kafka.protocol.types.Type;
import lombok.Builder;
import lombok.Data;

import static com.github.nginate.kafka.protocol.types.TypeName.INT32;
import static com.github.nginate.kafka.protocol.types.TypeName.WRAPPER;

@Data
@Builder
@ApiKey(ApiKeys.LEADER_AND_ISR)
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
