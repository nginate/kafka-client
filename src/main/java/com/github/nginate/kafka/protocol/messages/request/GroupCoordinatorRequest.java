package com.github.nginate.kafka.protocol.messages.request;

import com.github.nginate.kafka.serialization.ApiKey;
import com.github.nginate.kafka.protocol.KafkaApiKeys;
import com.github.nginate.kafka.serialization.Type;
import lombok.Builder;
import lombok.Data;

import static com.github.nginate.kafka.serialization.TypeName.STRING;

/**
 * The offsets for a given consumer group are maintained by a specific broker called the group coordinator. i.e., a
 * consumer needs to issue its offset commit and fetch requests to this specific broker. It can discover the current
 * coordinator by issuing a group coordinator request.
 */
@Data
@Builder
@ApiKey(KafkaApiKeys.GROUP_COORDINATOR)
public class GroupCoordinatorRequest {
    @Type(value = STRING, order = 4)
    private String groupId;
}
