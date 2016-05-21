package com.github.nginate.kafka.core;

import com.github.nginate.kafka.protocol.messages.dto.Broker;
import com.github.nginate.kafka.protocol.messages.response.TopicMetadataResponse;
import com.github.nginate.kafka.protocol.messages.response.TopicMetadataResponse.TopicMetadata.PartitionMetadata;
import lombok.Synchronized;

import javax.annotation.concurrent.ThreadSafe;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableList;

@ThreadSafe
class ClusterMetadata {
    private final List<Broker> clusterNodes = new ArrayList<>();
    private final Map<String, List<PartitionMetadata>> partitionsByTopic = new HashMap<>();
    private final Map<String, List<PartitionMetadata>> availablePartitionsByTopic = new HashMap<>();

    @Synchronized
    public void update(TopicMetadataResponse metadataResponse) {
        cleanAll();
        // make a randomized copy of the nodes
        List<Broker> copy = new ArrayList<>(Arrays.asList(metadataResponse.getBrokers()));
        Collections.shuffle(copy);
        clusterNodes.addAll(copy);

        stream(metadataResponse.getTopicMetadata()).forEach(topicMetadata -> {
            List<PartitionMetadata> partitionData = unmodifiableList(asList(topicMetadata.getPartitionMetadata()));
            partitionsByTopic.put(topicMetadata.getTopicName(), partitionData);

            partitionData.stream()
                    .filter(partitionMetadata -> partitionMetadata.getLeader() > 0)
                    .findAny().ifPresent(partitionMetadata -> {
                availablePartitionsByTopic.put(topicMetadata.getTopicName(), partitionData);
            });
        });
    }

    @Synchronized
    public List<Broker> getClusterNodes() {
        return unmodifiableList(clusterNodes);
    }

    @Synchronized
    public void clear() {
        cleanAll();
    }

    private void cleanAll() {
        clusterNodes.clear();
        partitionsByTopic.clear();
        availablePartitionsByTopic.clear();
    }
}
