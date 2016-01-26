package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.protocol.ApiKey;
import com.github.nginate.kafka.protocol.ApiKeys;
import com.github.nginate.kafka.protocol.types.Type;
import lombok.Data;

import static com.github.nginate.kafka.protocol.types.TypeName.BYTES;
import static com.github.nginate.kafka.protocol.types.TypeName.INT16;

/**
 * Each member in the group will receive an assignment from the leader in the sync group response.
 */
@Data
@ApiKey(ApiKeys.SYNC_GROUP)
public class SyncGroupResponse {
    @Type(value = INT16, order = 2)
    private Short errorCode;
    @Type(value = BYTES, order = 3)
    private byte[] memberAssignment;
}
