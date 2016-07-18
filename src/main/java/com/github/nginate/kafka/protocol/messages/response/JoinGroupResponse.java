package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.protocol.KafkaApiKeys;
import com.github.nginate.kafka.protocol.messages.HasRootErrorCode;
import com.github.nginate.kafka.serialization.ApiKey;
import com.github.nginate.kafka.serialization.ApiVersion;
import com.github.nginate.kafka.serialization.Type;
import lombok.Data;

import static com.github.nginate.kafka.serialization.TypeName.*;

/**
 * The JoinGroup response includes an array for the members of the group along with their metadata. This is only
 * populated for the leader to reduce the overall overhead of the protocol; for other members, it will be empty. The is
 * used by the leader to prepare member state for phase 2. In the case of the consumer, this allows the leader to
 * collect the subscriptions from all members and set the partition assignment. The member metadata returned in the join
 * group response corresponds to the respective metadata provided in the join group request for the group protocol
 * chosen by the coordinator.
 */
@Data
@ApiKey(KafkaApiKeys.JOIN_GROUP)
@ApiVersion(0)
public class JoinGroupResponse implements HasRootErrorCode {
    @Type(value = INT16, order = 2)
    private Short errorCode;
    @Type(value = INT32, order = 3)
    private Integer generationId;
    @Type(value = STRING, order = 4)
    private String groupProtocol;
    @Type(value = STRING, order = 5)
    private String leaderId;
    @Type(value = STRING, order = 6)
    private String memberId;
    @Type(value = WRAPPER, order = 7)
    private Member[] members;

    @Data
    public static class Member {
        @Type(STRING)
        private String memberId;
        @Type(value = BYTES, order = 1)
        private byte[] memberMetadata;
    }
}
