package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.protocol.KafkaApiKeys;
import com.github.nginate.kafka.serialization.ApiKey;
import com.github.nginate.kafka.serialization.ApiVersion;
import com.github.nginate.kafka.serialization.Type;
import lombok.Data;

import static com.github.nginate.kafka.serialization.TypeName.INT16;
import static com.github.nginate.kafka.serialization.TypeName.WRAPPER;

@Data
@ApiKey(KafkaApiKeys.API_VERSIONS)
@ApiVersion(0)
public class ApiVersionsResponse {
    @Type(INT16)
    private Short errorCode;
    /**
     * API versions supported by the broker.
     */
    @Type(value = WRAPPER, order = 1)
    private ApiVersions[] apiVersions;

    @Data
    public static class ApiVersions {
        /**
         * API key.
         */
        @Type(INT16)
        private Short apiKey;
        /**
         * Minimum supported version.
         */
        @Type(value = INT16, order = 1)
        private Short minVersion;
        /**
         * Maximum supported version.
         */
        @Type(value = INT16, order = 2)
        private Short maxVersion;
    }
}
