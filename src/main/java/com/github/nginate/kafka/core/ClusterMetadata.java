package com.github.nginate.kafka.core;

import com.github.nginate.kafka.exceptions.KafkaException;
import com.github.nginate.kafka.protocol.messages.dto.Broker;
import com.github.nginate.kafka.protocol.messages.response.TopicMetadataResponse;
import com.github.nginate.kafka.protocol.messages.response.TopicMetadataResponse.TopicMetadata.PartitionMetadata;
import com.github.nginate.kafka.util.CollectionUtils;
import lombok.Synchronized;

import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.function.Function.identity;

@ThreadSafe
class ClusterMetadata {
    private volatile Map<Integer, Broker> clusterNodes = new HashMap<>();
    private volatile Map<String, List<PartitionMetadata>> partitionsByTopic = new HashMap<>();
    private volatile Map<String, Broker> topicLeaders = new HashMap<>();

    public void update(TopicMetadataResponse metadataResponse) {
        clusterNodes = stream(metadataResponse.getBrokers()).collect(Collectors.toMap(Broker::getNodeId, identity()));

        Map<String, List<PartitionMetadata>> partitionsByTopic = new HashMap<>();
        Map<String, Broker> topicLeaders = new HashMap<>();

        stream(metadataResponse.getTopicMetadata()).forEach(topicMetadata -> {
            List<PartitionMetadata> partitionData = unmodifiableList(asList(topicMetadata.getPartitionMetadata()));
            partitionsByTopic.put(topicMetadata.getTopicName(), partitionData);

            partitionData.stream()
                    .filter(partitionMetadata -> partitionMetadata.getLeader() >= 0)
                    .findAny().ifPresent(partitionMetadata ->
                    topicLeaders.put(topicMetadata.getTopicName(), clusterNodes.get(partitionMetadata.getLeader())));
        });

        this.partitionsByTopic = partitionsByTopic;
        this.topicLeaders = topicLeaders;
    }

    public Optional<Broker> leaderFor(String topic) {
        return Optional.ofNullable(topicLeaders.get(topic));
    }

    public List<Integer> brokersForTopic(String topic) {
        return partitionsByTopic.getOrDefault(topic, emptyList())
                .stream()
                .flatMap(partitionMetadata -> stream(partitionMetadata.getReplicas()))
                .collect(Collectors.toList());
    }

    public Optional<Broker> brokerForId(Integer nodeId) {
        return Optional.ofNullable(clusterNodes.get(nodeId));
    }

    public Broker randomBroker() {
        List<Broker> nodes = new ArrayList<>(clusterNodes.values());

        if (nodes.isEmpty()) {
            return null;
        }

        Collections.shuffle(nodes);
        return nodes.iterator().next();
    }
}
