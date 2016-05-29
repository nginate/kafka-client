package com.github.nginate.kafka.protocol.messages.request;

import com.github.nginate.kafka.protocol.KafkaApiKeys;
import com.github.nginate.kafka.serialization.ApiKey;
import com.github.nginate.kafka.serialization.ApiVersion;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ApiKey(KafkaApiKeys.API_VERSIONS)
@ApiVersion(0)
public class ApiVersionsRequest {
}
