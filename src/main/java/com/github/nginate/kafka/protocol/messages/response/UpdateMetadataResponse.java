package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.protocol.ApiKey;
import com.github.nginate.kafka.protocol.ApiKeys;
import com.github.nginate.kafka.protocol.types.Type;
import com.github.nginate.kafka.protocol.types.TypeName;
import lombok.Data;

@Data
@ApiKey(ApiKeys.UPDATE_METADATA)
public class UpdateMetadataResponse {
    @Type(TypeName.INT16)
    private Short errorCode;
}
