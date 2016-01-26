package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.protocol.ApiKey;
import com.github.nginate.kafka.protocol.ApiKeys;
import com.github.nginate.kafka.protocol.types.Type;
import lombok.Data;

import static com.github.nginate.kafka.protocol.types.TypeName.*;

@Data
@ApiKey(ApiKeys.LIST_GROUPS)
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
