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
    @Type(value = INT16, order = 2)
    private Short errorCode;
    @Type(value = INT32, order = 3)
    private Integer coordinatorId;
    @Type(value = STRING, order = 4)
    private String coordinatorHost;
    @Type(value = INT32, order = 5)
    private Integer coordinatorPort;
}
