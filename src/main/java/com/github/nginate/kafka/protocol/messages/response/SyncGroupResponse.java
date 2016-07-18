package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.protocol.KafkaApiKeys;
import com.github.nginate.kafka.protocol.messages.HasRootErrorCode;
import com.github.nginate.kafka.serialization.ApiKey;
import com.github.nginate.kafka.serialization.ApiVersion;
import com.github.nginate.kafka.serialization.Type;
import lombok.Data;

import static com.github.nginate.kafka.serialization.TypeName.BYTES;
import static com.github.nginate.kafka.serialization.TypeName.INT16;

/**
 * Each member in the group will receive an assignment from the leader in the sync group response.
 */
@Data
@ApiKey(KafkaApiKeys.SYNC_GROUP)
@ApiVersion(0)
public class SyncGroupResponse implements HasRootErrorCode {
    @Type(value = INT16, order = 2)
    private Short errorCode;
    @Type(value = BYTES, order = 3)
    private byte[] memberAssignment;
}
