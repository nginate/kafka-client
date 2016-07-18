package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.protocol.KafkaApiKeys;
import com.github.nginate.kafka.protocol.messages.HasRootErrorCode;
import com.github.nginate.kafka.protocol.messages.dto.TopicAndPartitionStatus;
import com.github.nginate.kafka.serialization.ApiKey;
import com.github.nginate.kafka.serialization.ApiVersion;
import com.github.nginate.kafka.serialization.Type;
import lombok.Data;

import static com.github.nginate.kafka.serialization.TypeName.INT16;
import static com.github.nginate.kafka.serialization.TypeName.WRAPPER;

@Data
@ApiKey(KafkaApiKeys.LEADER_AND_ISR)
@ApiVersion(0)
public class LeaderAndIsrResponse implements HasRootErrorCode {
    @Type(value = INT16, order = 2)
    private Short errorCode;
    @Type(value = WRAPPER, order = 3)
    private TopicAndPartitionStatus[] statuses;
}
