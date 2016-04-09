package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.protocol.ApiKey;
import com.github.nginate.kafka.protocol.ApiKeys;
import com.github.nginate.kafka.protocol.messages.dto.TopicAndPartition;
import com.github.nginate.kafka.protocol.types.Type;
import com.github.nginate.kafka.protocol.types.TypeName;
import lombok.Data;

@Data
@ApiKey(ApiKeys.DESCRIBE_GROUPS)
public class ControlledShutdownResponse {
    @Type(TypeName.INT16)
    private Short errorCode;
    @Type(value = TypeName.WRAPPER, order = 1)
    private TopicAndPartition[] remainingPartitions;
}
