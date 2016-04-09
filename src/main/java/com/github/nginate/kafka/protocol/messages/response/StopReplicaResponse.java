package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.protocol.ApiKey;
import com.github.nginate.kafka.protocol.ApiKeys;
import com.github.nginate.kafka.protocol.messages.dto.TopicAndPartitionStatus;
import com.github.nginate.kafka.protocol.types.Type;
import com.github.nginate.kafka.protocol.types.TypeName;
import lombok.Builder;
import lombok.Data;

@Data
@ApiKey(ApiKeys.STOP_REPLICA)
public class StopReplicaResponse {
    @Type(value = TypeName.INT16)
    private Short errorCode;
    @Type(value = TypeName.WRAPPER, order = 1)
    private TopicAndPartitionStatus[] statuses;
}
