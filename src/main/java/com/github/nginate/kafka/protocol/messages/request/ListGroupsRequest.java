package com.github.nginate.kafka.protocol.messages.request;

import com.github.nginate.kafka.serialization.ApiKey;
import com.github.nginate.kafka.protocol.KafkaApiKeys;
import lombok.Builder;
import lombok.Data;

/**
 * This API can be used to find the current groups managed by a broker. To get a list of all groups in the cluster, you
 * must send ListGroup to all brokers.
 */
@Data
@Builder
@ApiKey(KafkaApiKeys.LIST_GROUPS)
public class ListGroupsRequest {
}
