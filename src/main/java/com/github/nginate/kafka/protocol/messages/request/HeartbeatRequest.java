package com.github.nginate.kafka.protocol.messages.request;

import com.github.nginate.kafka.serialization.ApiKey;
import com.github.nginate.kafka.protocol.KafkaApiKeys;
import com.github.nginate.kafka.serialization.Type;
import lombok.Builder;
import lombok.Data;

import static com.github.nginate.kafka.serialization.TypeName.INT32;
import static com.github.nginate.kafka.serialization.TypeName.STRING;

/**
 * Once a member has joined and synced, it will begin sending periodic heartbeats to keep itself in the group. If not
 * heartbeat has been received by the coordinator with the configured session timeout, the member will be kicked out of
 * the group.
 */
@Data
@Builder
@ApiKey(KafkaApiKeys.HEARTBEAT)
public class HeartbeatRequest {
    @Type(value = STRING, order = 4)
    private String groupId;
    @Type(value = INT32, order = 5)
    private Integer generationId;
    @Type(value = STRING, order = 6)
    private String memberId;
}
