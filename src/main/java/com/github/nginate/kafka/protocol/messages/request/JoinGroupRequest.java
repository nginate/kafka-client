package com.github.nginate.kafka.protocol.messages.request;

import com.github.nginate.kafka.protocol.ApiKey;
import com.github.nginate.kafka.protocol.ApiKeys;
import com.github.nginate.kafka.protocol.messages.Request;
import com.github.nginate.kafka.protocol.types.Type;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.github.nginate.kafka.protocol.types.TypeName.*;

/**
 * The purpose of the initial phase is to set the active members of the group. This protocol has similar semantics as in
 * the initial consumer rewrite design. After finding the coordinator for the group, each member sends a JoinGroup
 * request containing member-specific metadata. The join group request will park at the coordinator until all expected
 * members have sent their own join group requests ("expected" in this case means all members that were part of the
 * previous generation). Once they have done so, the coordinator randomly selects a leader from the group and sends
 * JoinGroup responses to all the pending requests.
 * The JoinGroup request contains an array with the group protocols that it supports along with member-specific
 * metadata. This is basically used to ensure compatibility of group member metadata within the group. The coordinator
 * chooses a protocol which is supported by all members of the group and returns it in the respective JoinGroup
 * responses. If a member joins and doesn't support any of the protocols used by the rest of the group, then it will be
 * rejected. This mechanism provides a way to update protocol metadata to a new format in a rolling upgrade scenario.
 * The newer version will provide metadata for the new protocol and for the old protocol, and the coordinator will
 * choose the old protocol until all members have been upgraded.
 * The JoinGroup response includes an array for the members of the group along with their metadata. This is only
 * populated for the leader to reduce the overall overhead of the protocol; for other members, it will be empty. The is
 * used by the leader to prepare member state for phase 2. In the case of the consumer, this allows the leader to
 * collect the subscriptions from all members and set the partition assignment. The member metadata returned in the join
 * group response corresponds to the respective metadata provided in the join group request for the group protocol
 * chosen by the coordinator.
 */
@Data
@Builder
@ApiKey(ApiKeys.JOIN_GROUP)
@EqualsAndHashCode(callSuper = true)
public class JoinGroupRequest extends Request {
    @Type(value = STRING, order = 4)
    private String groupId;
    @Type(value = INT32, order = 5)
    private Integer sessionTimeout;
    @Type(value = STRING, order = 6)
    private String memberId;
    @Type(value = STRING, order = 7)
    private String protocolType;
    @Type(value = WRAPPER, order = 8)
    private GroupProtocols[] groupProtocols;

    @Data
    public static class GroupProtocols {
        @Type(STRING)
        private String protocolType;
        @Type(value = BYTES, order = 1)
        private byte[] protocolMetadata;
    }
}
