package com.github.nginate.kafka.protocol.messages.request;

import com.github.nginate.kafka.protocol.KafkaApiKeys;
import com.github.nginate.kafka.serialization.ApiKey;
import com.github.nginate.kafka.serialization.ApiVersion;
import com.github.nginate.kafka.serialization.Type;
import lombok.Builder;
import lombok.Data;

import static com.github.nginate.kafka.serialization.TypeName.STRING;

/**
 * This API answers the following questions:
 * What topics exist?
 * How many partitions does each topic have?
 * Which broker is currently the leader for each partition?
 * What is the host and port for each of these brokers?
 * This is the only request that can be addressed to any broker in the cluster. Since there may be many topics the
 * client can give an optional list of topic names in order to only return metadata for a subset of topics. The metadata
 * returned is at the partition level, but grouped together by topic for convenience and to avoid redundancy. For each
 * partition the metadata contains the information for the leader as well as for all the replicas and the list of
 * replicas that are currently in-sync. Note: If "auto.create.topics.enable" is set in the broker configuration, a topic
 * metadata request will create the topic with the default replication factor and number of partitions.
 */
@Data
@Builder
@ApiKey(KafkaApiKeys.METADATA)
@ApiVersion(1)
public class TopicMetadataRequest {
    /**
     * An array of topics to fetch metadata for. If the topics array is null fetch metadata for all topics.
     */
    @Type(value = STRING, order = 4)
    private String[] topics;
}
