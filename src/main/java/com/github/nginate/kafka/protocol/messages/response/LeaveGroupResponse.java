package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.protocol.messages.Response;
import com.github.nginate.kafka.protocol.types.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.github.nginate.kafka.protocol.types.TypeName.INT16;

@Data
//@ApiKey(ApiKeys.) FIXME
@EqualsAndHashCode(callSuper = true)
public class LeaveGroupResponse extends Response {
    @Type(INT16)
    private Short errorCode;
}
