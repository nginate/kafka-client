package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.serialization.ApiKey;
import com.github.nginate.kafka.protocol.KafkaApiKeys;
import com.github.nginate.kafka.protocol.messages.dto.TopicAndPartition;
import com.github.nginate.kafka.serialization.Type;
import com.github.nginate.kafka.serialization.TypeName;
import lombok.Data;

@Data
@ApiKey(KafkaApiKeys.DESCRIBE_GROUPS)
public class ControlledShutdownResponse {
    @Type(TypeName.INT16)
    private Short errorCode;
    @Type(value = TypeName.WRAPPER, order = 1)
    private TopicAndPartition[] remainingPartitions;
}
