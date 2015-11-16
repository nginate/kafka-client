package com.github.nginate.kafka.protocol.messages.request;

import com.github.nginate.kafka.protocol.ApiKey;
import com.github.nginate.kafka.protocol.ApiKeys;
import com.github.nginate.kafka.protocol.messages.Request;
import com.github.nginate.kafka.protocol.types.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.github.nginate.kafka.protocol.types.TypeName.STRING;

@Data
@ApiKey(ApiKeys.METADATA)
@EqualsAndHashCode(callSuper = true)
public class TopicMetadataRequest extends Request {
    /**
     * The topics to produce metadata for. If empty the request will yield metadata for all topics.
     */
    @Type(STRING)
    private String[] topic;
}
