package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.protocol.KafkaApiKeys;
import com.github.nginate.kafka.protocol.messages.HasRootErrorCode;
import com.github.nginate.kafka.serialization.ApiKey;
import com.github.nginate.kafka.serialization.ApiVersion;
import com.github.nginate.kafka.serialization.Type;
import lombok.Data;

import static com.github.nginate.kafka.serialization.TypeName.INT16;
import static com.github.nginate.kafka.serialization.TypeName.STRING;

@Data
@ApiKey(KafkaApiKeys.SASL_HANDSHAKE)
@ApiVersion(0)
public class SASLHanshakeResponse implements HasRootErrorCode {
    @Type(INT16)
    private Short errorCode;
    /**
     * Array of mechanisms enabled in the server.
     */
    @Type(value = STRING, order = 1)
    private String[] enabledMechanisms;
}
