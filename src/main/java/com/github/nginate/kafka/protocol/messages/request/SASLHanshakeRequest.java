package com.github.nginate.kafka.protocol.messages.request;

import com.github.nginate.kafka.protocol.KafkaApiKeys;
import com.github.nginate.kafka.serialization.ApiKey;
import com.github.nginate.kafka.serialization.ApiVersion;
import com.github.nginate.kafka.serialization.Type;
import lombok.Builder;
import lombok.Data;

import static com.github.nginate.kafka.serialization.TypeName.STRING;

@Data
@Builder
@ApiKey(KafkaApiKeys.SASL_HANDSHAKE)
@ApiVersion(0)
public class SASLHanshakeRequest {
    /**
     * SASL Mechanism chosen by the client.
     */
    @Type(STRING)
    private String mechanism;
}
