package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.protocol.ApiKey;
import com.github.nginate.kafka.protocol.ApiKeys;
import com.github.nginate.kafka.protocol.messages.Response;
import com.github.nginate.kafka.protocol.types.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.github.nginate.kafka.protocol.types.TypeName.*;

/**
 * The JoinGroup response includes an array for the members of the group along with their metadata. This is only
 * populated for the leader to reduce the overall overhead of the protocol; for other members, it will be empty. The is
 * used by the leader to prepare member state for phase 2. In the case of the consumer, this allows the leader to
 * collect the subscriptions from all members and set the partition assignment. The member metadata returned in the join
 * group response corresponds to the respective metadata provided in the join group request for the group protocol
 * chosen by the coordinator.
 */
@Data
@ApiKey(ApiKeys.JOIN_GROUP)
@EqualsAndHashCode(callSuper = true)
public class JoinGroupResponse extends Response {
    @Type(INT16)
    private Short errorCode;
    @Type(INT32)
    private Integer generationId;
    @Type(STRING)
    private String groupProtocol;
    @Type(STRING)
    private String leaderId;
    @Type(STRING)
    private String memberId;
    @Type(WRAPPER)
    private Member[] members;

    @Data
    public static class Member{
        @Type(STRING)
        private String memberId;
        @Type(BYTES)
        private byte[] memberMetadata;
    }
}
