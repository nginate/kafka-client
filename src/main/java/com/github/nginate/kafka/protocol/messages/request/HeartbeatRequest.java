package com.github.nginate.kafka.protocol.messages.request;

import com.github.nginate.kafka.protocol.ApiKey;
import com.github.nginate.kafka.protocol.ApiKeys;
import com.github.nginate.kafka.protocol.messages.Request;
import com.github.nginate.kafka.protocol.types.Type;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.github.nginate.kafka.protocol.types.TypeName.INT32;
import static com.github.nginate.kafka.protocol.types.TypeName.STRING;

/**
 * Once a member has joined and synced, it will begin sending periodic heartbeats to keep itself in the group. If not
 * heartbeat has been received by the coordinator with the configured session timeout, the member will be kicked out of
 * the group.
 */
@Data
@Builder
@ApiKey(ApiKeys.HEARTBEAT)
@EqualsAndHashCode(callSuper = true)
public class HeartbeatRequest extends Request {
    @Type(STRING)
    private String groupId;
    @Type(INT32)
    private Integer generationId;
    @Type(STRING)
    private String memberId;
}
