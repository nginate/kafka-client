package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.protocol.KafkaApiKeys;
import com.github.nginate.kafka.protocol.messages.HasRootErrorCode;
import com.github.nginate.kafka.protocol.messages.dto.Broker;
import com.github.nginate.kafka.serialization.ApiKey;
import com.github.nginate.kafka.serialization.ApiVersion;
import com.github.nginate.kafka.serialization.Type;
import lombok.Data;

import static com.github.nginate.kafka.serialization.TypeName.*;

@Data
@ApiKey(KafkaApiKeys.GROUP_COORDINATOR)
@ApiVersion(0)
public class GroupCoordinatorResponse implements HasRootErrorCode {
    @Type(value = INT16, order = 2)
    private Short errorCode;
    /**
     * Host and port information for the coordinator for a consumer group.
     */
    @Type(value = WRAPPER, order = 3)
    private Broker coordinator;
}
