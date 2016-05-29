package com.github.nginate.kafka.protocol.messages.request;

import com.github.nginate.kafka.protocol.KafkaApiKeys;
import com.github.nginate.kafka.serialization.ApiKey;
import com.github.nginate.kafka.serialization.ApiVersion;
import com.github.nginate.kafka.serialization.Type;
import lombok.Builder;
import lombok.Data;

import static com.github.nginate.kafka.serialization.TypeName.*;

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
@ApiKey(KafkaApiKeys.SYNC_GROUP)
@ApiVersion(0)
public class SyncGroupRequest {
    @Type(value = STRING, order = 4)
    private String groupId;
    @Type(value = INT32, order = 5)
    private Integer generationId;
    @Type(value = STRING, order = 6)
    private String memberId;
    @Type(value = WRAPPER, order = 7)
    private GroupAssignment[] groupAssignments;

    @Data
    public static class GroupAssignment {
        @Type(STRING)
        private String memberId;
        @Type(value = BYTES, order = 1)
        private byte[] memberAssignment;
    }
}
