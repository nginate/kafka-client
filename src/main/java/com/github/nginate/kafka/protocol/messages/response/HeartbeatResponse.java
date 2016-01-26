package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.protocol.ApiKey;
import com.github.nginate.kafka.protocol.ApiKeys;
import com.github.nginate.kafka.protocol.types.Type;
import lombok.Data;

import static com.github.nginate.kafka.protocol.types.TypeName.INT16;

@Data
@ApiKey(ApiKeys.HEARTBEAT)
public class HeartbeatResponse {
    @Type(value = INT16, order = 2)
    private Short errorCode;
}
