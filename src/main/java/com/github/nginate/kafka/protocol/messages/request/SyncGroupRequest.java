package com.github.nginate.kafka.protocol.messages.request;

import com.github.nginate.kafka.protocol.messages.Request;
import com.github.nginate.kafka.protocol.types.Type;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.github.nginate.kafka.protocol.types.TypeName.*;

/**
 * The sync group request is used by the leader to assign state (e.g. partition assignments) to all members of the
 * current generation. All members send SyncGroup immediately after joining the group, but only the leader provides the
 * group's assignment.
 * Once the group members have been stabilized by the completion of phase 1, the active leader must propagate state to
 * the other members in the group. This is used in the new consumer protocol to set partition assignments. Similar to
 * phase 1, all members send SyncGroup requests to the coordinator. Once group state has been provided by the leader,
 * the coordinator forwards each member's state respectively in the SyncGroup response.
 */
@Data
@Builder
//@ApiKey(ApiKeys.) FIXME
@EqualsAndHashCode(callSuper = true)
public class SyncGroupRequest extends Request {
    @Type(STRING)
    private String groupId;
    @Type(INT32)
    private Integer generationId;
    @Type(STRING)
    private String memberId;
    @Type(WRAPPER)
    private GroupAssignment[] groupAssignments;

    @Data
    public static class GroupAssignment {
        @Type(STRING)
        private String memberId;
        @Type(BYTES)
        private byte[] memberAssignment;
    }
}
