package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.protocol.messages.Response;
import com.github.nginate.kafka.protocol.types.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.github.nginate.kafka.protocol.types.TypeName.INT16;
import static com.github.nginate.kafka.protocol.types.TypeName.STRING;
import static com.github.nginate.kafka.protocol.types.TypeName.WRAPPER;

@Data
//@ApiKey(ApiKeys.) FIXME
@EqualsAndHashCode(callSuper = true)
public class ListGroupsResponse extends Response {
    @Type(INT16)
    private Short errorCode;
    @Type(WRAPPER)
    private Group[] groups;

    @Data
    public static class Group {
        @Type(STRING)
        private String groupId;
        @Type(STRING)
        private String protocolType;
    }
}
