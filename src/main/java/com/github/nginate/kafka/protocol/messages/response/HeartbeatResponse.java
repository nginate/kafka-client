package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.protocol.ApiKey;
import com.github.nginate.kafka.protocol.ApiKeys;
import com.github.nginate.kafka.protocol.messages.Response;
import com.github.nginate.kafka.protocol.types.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.github.nginate.kafka.protocol.types.TypeName.INT16;

@Data
@ApiKey(ApiKeys.HEARTBEAT)
@EqualsAndHashCode(callSuper = true)
public class HeartbeatResponse extends Response {
    @Type(value = INT16, order = 2)
    private Short errorCode;
}
