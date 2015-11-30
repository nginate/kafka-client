package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.protocol.ApiKey;
import com.github.nginate.kafka.protocol.ApiKeys;
import com.github.nginate.kafka.protocol.messages.Response;
import com.github.nginate.kafka.protocol.types.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.github.nginate.kafka.protocol.types.TypeName.*;

@Data
@ApiKey(ApiKeys.GROUP_COORDINATOR)
@EqualsAndHashCode(callSuper = true)
public class GroupCoordinatorResponse extends Response {
    @Type(INT16)
    private Short errorCode;
    @Type(INT32)
    private Integer coordinatorId;
    @Type(STRING)
    private String coordinatorHost;
    @Type(INT32)
    private Integer coordinatorPort;
}
