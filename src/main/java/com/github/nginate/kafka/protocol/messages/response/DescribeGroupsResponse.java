package com.github.nginate.kafka.protocol.messages.response;

import com.github.nginate.kafka.protocol.KafkaApiKeys;
import com.github.nginate.kafka.serialization.ApiKey;
import com.github.nginate.kafka.serialization.ApiVersion;
import com.github.nginate.kafka.serialization.Type;
import lombok.Data;

import static com.github.nginate.kafka.serialization.TypeName.*;

@Data
@ApiKey(KafkaApiKeys.DESCRIBE_GROUPS)
@ApiVersion(0)
public class DescribeGroupsResponse {
    @Type(value = WRAPPER, order = 2)
    private DescribeGroupsResponseData[] groupData;

    @Data
    public static class DescribeGroupsResponseData {
        @Type(INT16)
        private Short errorCode;
        @Type(value = STRING, order = 1)
        private String groupId;
        @Type(value = STRING, order = 2)
        private String state;
        @Type(value = STRING, order = 3)
        private String protocolType;
        @Type(value = STRING, order = 4)
        private String protocol;
        @Type(value = WRAPPER, order = 5)
        private DescribeGroupResponseMemberData[] memberData;

        @Data
        public static class DescribeGroupResponseMemberData {
            @Type(STRING)
            private String memberId;
            @Type(value = STRING, order = 1)
            private String clientId;
            @Type(value = STRING, order = 2)
            private String clientHost;
            @Type(value = BYTES, order = 3)
            private byte[] memberMetadata;
            @Type(value = BYTES, order = 4)
            private byte[] memberAssignment;
        }
    }
}
