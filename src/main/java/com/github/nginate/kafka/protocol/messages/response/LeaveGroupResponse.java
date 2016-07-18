package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.protocol.KafkaApiKeys;
import com.github.nginate.kafka.protocol.messages.HasRootErrorCode;
import com.github.nginate.kafka.serialization.ApiKey;
import com.github.nginate.kafka.serialization.ApiVersion;
import com.github.nginate.kafka.serialization.Type;
import lombok.Data;

import static com.github.nginate.kafka.serialization.TypeName.INT16;

@Data
@ApiKey(KafkaApiKeys.LEAVE_GROUP)
@ApiVersion(0)
public class LeaveGroupResponse implements HasRootErrorCode {
    @Type(value = INT16, order = 2)
    private Short errorCode;
}
