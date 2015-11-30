package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.protocol.messages.Response;
import com.github.nginate.kafka.protocol.types.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.github.nginate.kafka.protocol.types.TypeName.*;

@Data
//@ApiKey(ApiKeys.) FIXME
@EqualsAndHashCode(callSuper = true)
public class DescribeGroupsResponse extends Response {
    @Type(WRAPPER)
    private DescribeGroupsResponseData[] groupData;

    @Data
    public static class DescribeGroupsResponseData {
        @Type(INT16)
        private Short errorCode;
        @Type(STRING)
        private String groupId;
        @Type(STRING)
        private String state;
        @Type(STRING)
        private String protocolType;
        @Type(STRING)
        private String protocol;
        @Type(WRAPPER)
        private DescribeGroupResponseMemberData[] memberData;

        @Data
        public static class DescribeGroupResponseMemberData {
            @Type(STRING)
            private String memberId;
            @Type(STRING)
            private String clientId;
            @Type(STRING)
            private String clientHost;
            @Type(BYTES)
            private byte[] memberMetadata;
            @Type(BYTES)
            private byte[] memberAssignment;
        }
    }
}
