package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.protocol.KafkaApiKeys;
import com.github.nginate.kafka.serialization.ApiKey;
import com.github.nginate.kafka.serialization.ApiVersion;
import com.github.nginate.kafka.serialization.Type;
import lombok.Data;

import static com.github.nginate.kafka.serialization.TypeName.*;

@Data
@ApiKey(KafkaApiKeys.LIST_GROUPS)
@ApiVersion(0)
public class ListGroupsResponse {
    @Type(value = INT16, order = 2)
    private Short errorCode;
    @Type(value = WRAPPER, order = 3)
    private Group[] groups;

    @Data
    public static class Group {
        @Type(STRING)
        private String groupId;
        @Type(value = STRING, order = 1)
        private String protocolType;
    }
}
