package com.github.nginate.kafka.protocol.messages.request;

import com.github.nginate.kafka.protocol.ApiKey;
import com.github.nginate.kafka.protocol.ApiKeys;
import com.github.nginate.kafka.protocol.messages.dto.TopicAndPartition;
import com.github.nginate.kafka.protocol.types.Type;
import com.github.nginate.kafka.protocol.types.TypeName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ApiKey(ApiKeys.STOP_REPLICA)
public class StopReplicaRequest {
    @Type(value = TypeName.INT32, order = 4)
    private Integer controllerId;
    @Type(value = TypeName.INT32, order = 5)
    private Integer controllerEpoch;
    @Type(value = TypeName.INT8, order = 6)
    private Byte deletePartitions;
    @Type(value = TypeName.WRAPPER, order = 7)
    private TopicAndPartition[] topicsAndPartitions;
}
