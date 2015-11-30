package com.github.nginate.kafka.protocol.messages.request;

import com.github.nginate.kafka.protocol.messages.Request;
import com.github.nginate.kafka.protocol.types.Type;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.github.nginate.kafka.protocol.types.TypeName.STRING;

@Data
@Builder
//@ApiKey(ApiKeys.) FIXME
@EqualsAndHashCode(callSuper = true)
public class DescribeGroupsRequest extends Request {
    @Type(STRING)
    private String[] groupIds;
}
