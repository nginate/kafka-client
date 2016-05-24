package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.serialization.ApiKey;
import com.github.nginate.kafka.protocol.KafkaApiKeys;
import com.github.nginate.kafka.protocol.messages.dto.TopicAndPartitionStatus;
import com.github.nginate.kafka.serialization.Type;
import lombok.Data;

import static com.github.nginate.kafka.serialization.TypeName.*;

@Data
@ApiKey(KafkaApiKeys.LEADER_AND_ISR)
public class LeaderAndIsrResponse {
    @Type(value = INT16, order = 2)
    private Short errorCode;
    @Type(value = WRAPPER, order = 3)
    private TopicAndPartitionStatus[] statuses;
}
