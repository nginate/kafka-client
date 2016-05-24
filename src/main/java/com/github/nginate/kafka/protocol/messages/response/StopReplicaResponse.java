package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.serialization.ApiKey;
import com.github.nginate.kafka.protocol.KafkaApiKeys;
import com.github.nginate.kafka.protocol.messages.dto.TopicAndPartitionStatus;
import com.github.nginate.kafka.serialization.Type;
import com.github.nginate.kafka.serialization.TypeName;
import lombok.Data;

@Data
@ApiKey(KafkaApiKeys.STOP_REPLICA)
public class StopReplicaResponse {
    @Type(value = TypeName.INT16)
    private Short errorCode;
    @Type(value = TypeName.WRAPPER, order = 1)
    private TopicAndPartitionStatus[] statuses;
}
