package com.github.nginate.kafka.protocol.messages.request;

import com.github.nginate.kafka.protocol.messages.Request;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * This API can be used to find the current groups managed by a broker. To get a list of all groups in the cluster, you
 * must send ListGroup to all brokers.
 */
@Data
@Builder
//@ApiKey(ApiKeys.) FIXME
@EqualsAndHashCode(callSuper = true)
public class ListGroupsRequest extends Request {
}
