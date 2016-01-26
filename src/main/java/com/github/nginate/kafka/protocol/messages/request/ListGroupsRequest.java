package com.github.nginate.kafka.protocol.messages.request;

import com.github.nginate.kafka.protocol.ApiKey;
import com.github.nginate.kafka.protocol.ApiKeys;
import lombok.Builder;
import lombok.Data;

/**
 * This API can be used to find the current groups managed by a broker. To get a list of all groups in the cluster, you
 * must send ListGroup to all brokers.
 */
@Data
@Builder
@ApiKey(ApiKeys.LIST_GROUPS)
public class ListGroupsRequest {
}
