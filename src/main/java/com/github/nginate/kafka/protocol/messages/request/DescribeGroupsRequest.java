package com.github.nginate.kafka.protocol.messages.request;

import com.github.nginate.kafka.protocol.ApiKey;
import com.github.nginate.kafka.protocol.ApiKeys;
import com.github.nginate.kafka.protocol.types.Type;
import lombok.Builder;
import lombok.Data;

import static com.github.nginate.kafka.protocol.types.TypeName.STRING;

@Data
@Builder
@ApiKey(ApiKeys.DESCRIBE_GROUPS)
public class DescribeGroupsRequest {
    @Type(value = STRING, order = 4)
    private String[] groupIds;
}
