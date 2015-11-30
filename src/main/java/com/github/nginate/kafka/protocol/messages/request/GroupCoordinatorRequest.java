package com.github.nginate.kafka.protocol.messages.request;

import com.github.nginate.kafka.protocol.ApiKey;
import com.github.nginate.kafka.protocol.ApiKeys;
import com.github.nginate.kafka.protocol.messages.Request;
import com.github.nginate.kafka.protocol.types.Type;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.github.nginate.kafka.protocol.types.TypeName.STRING;

/**
 * The offsets for a given consumer group are maintained by a specific broker called the group coordinator. i.e., a
 * consumer needs to issue its offset commit and fetch requests to this specific broker. It can discover the current
 * coordinator by issuing a group coordinator request.
 */
@Data
@Builder
@ApiKey(ApiKeys.GROUP_COORDINATOR)
@EqualsAndHashCode(callSuper = true)
public class GroupCoordinatorRequest extends Request {
    @Type(STRING)
    private String groupId;
}
