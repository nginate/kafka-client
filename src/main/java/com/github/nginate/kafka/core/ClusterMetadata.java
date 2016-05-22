package com.github.nginate.kafka.core;

import com.github.nginate.kafka.exceptions.KafkaException;
import com.github.nginate.kafka.protocol.messages.dto.Broker;
import com.github.nginate.kafka.protocol.messages.response.TopicMetadataResponse;
import com.github.nginate.kafka.protocol.messages.response.TopicMetadataResponse.TopicMetadata.PartitionMetadata;
import com.github.nginate.kafka.util.CollectionUtils;
import lombok.Synchronized;

import javax.annotation.concurrent.ThreadSafe;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableList;

@ThreadSafe
class ClusterMetadata {
    private final Map<Integer, Broker> clusterNodes = new HashMap<>();
    private final Map<String, List<PartitionMetadata>> partitionsByTopic = new HashMap<>();
    private final Map<String, Broker> topicLeaders = new HashMap<>();
    private final Map<String, List<PartitionMetadata>> availablePartitionsByTopic = new HashMap<>();

    @Synchronized
    public void update(TopicMetadataResponse metadataResponse) {
        cleanAll();

        for (Broker broker : metadataResponse.getBrokers()) {
            clusterNodes.put(broker.getNodeId(), broker);
        }

        stream(metadataResponse.getTopicMetadata()).forEach(topicMetadata -> {
            List<PartitionMetadata> partitionData = unmodifiableList(asList(topicMetadata.getPartitionMetadata()));
            partitionsByTopic.put(topicMetadata.getTopicName(), partitionData);

            partitionData.stream()
                    .filter(partitionMetadata -> partitionMetadata.getLeader() >= 0)
                    .findAny().ifPresent(partitionMetadata -> {
                availablePartitionsByTopic.put(topicMetadata.getTopicName(), partitionData);
                topicLeaders.put(topicMetadata.getTopicName(), clusterNodes.get(partitionMetadata.getLeader()));
            });
        });
    }

    private void cleanAll() {
        clusterNodes.clear();
        topicLeaders.clear();
        partitionsByTopic.clear();
        availablePartitionsByTopic.clear();
    }

    @Synchronized
    public Broker leaderFor(String topic) {
        return topicLeaders.get(topic);
    }

    @Synchronized
    public Broker randomBroker() {
        List<Broker> nodes = new ArrayList<>(clusterNodes.values());
        Collections.shuffle(nodes);
        if (nodes.isEmpty()) {
            throw new KafkaException("There are no nodes in this cluster");
        }
        return nodes.iterator().next();
    }
}
