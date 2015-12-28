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
 * To explicitly leave a group, the client can send a leave group request. This is preferred over letting the session
 * timeout expire since it allows the group to rebalance faster, which for the consumer means that less time will elapse
 * before partitions can be reassigned to an active member.
 */
@Data
@Builder
@ApiKey(ApiKeys.LEAVE_GROUP)
@EqualsAndHashCode(callSuper = true)
public class LeaveGroupRequest extends Request {
    @Type(value = STRING, order = 4)
    private String groupId;
    @Type(value = STRING, order = 5)
    private String memberId;
}
