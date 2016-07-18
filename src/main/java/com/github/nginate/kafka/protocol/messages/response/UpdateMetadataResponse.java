package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.protocol.KafkaApiKeys;
import com.github.nginate.kafka.protocol.messages.HasRootErrorCode;
import com.github.nginate.kafka.serialization.ApiKey;
import com.github.nginate.kafka.serialization.ApiVersion;
import com.github.nginate.kafka.serialization.Type;
import com.github.nginate.kafka.serialization.TypeName;
import lombok.Data;

@Data
@ApiKey(KafkaApiKeys.UPDATE_METADATA)
@ApiVersion(2)
public class UpdateMetadataResponse implements HasRootErrorCode {
    @Type(TypeName.INT16)
    private Short errorCode;
}
