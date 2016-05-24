package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.serialization.ApiKey;
import com.github.nginate.kafka.protocol.KafkaApiKeys;
import com.github.nginate.kafka.protocol.messages.dto.Broker;
import com.github.nginate.kafka.serialization.Type;
import lombok.Data;

import static com.github.nginate.kafka.serialization.TypeName.*;

@Data
@ApiKey(KafkaApiKeys.GROUP_COORDINATOR)
public class GroupCoordinatorResponse {
    @Type(value = INT16, order = 2)
    private Short errorCode;
    @Type(value = WRAPPER, order = 3)
    private Broker coordinator;
}
