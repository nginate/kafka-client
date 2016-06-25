package com.github.nginate.kafka.core;

import com.github.nginate.kafka.protocol.messages.response.GroupCoordinatorResponse.GroupCoordinatorBroker;
import com.github.nginate.kafka.protocol.messages.response.TopicMetadataResponse;
import com.github.nginate.kafka.protocol.messages.response.TopicMetadataResponse.TopicMetadata.PartitionMetadata;
import com.github.nginate.kafka.protocol.messages.response.TopicMetadataResponse.TopicMetadataBroker;

import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@ThreadSafe
class ClusterMetadata {
    private volatile Map<Integer, TopicMetadataBroker> clusterNodes = new HashMap<>();
    private volatile Map<String, List<PartitionMetadata>> partitionsByTopic = new HashMap<>();
    private volatile Map<String, TopicMetadataBroker> topicLeaders = new HashMap<>();
    private final Map<String, GroupCoordinatorBroker> groupCoordinators = new ConcurrentHashMap<>();
    private final Map<String, String> groupMemberIds = new ConcurrentHashMap<>();
    private volatile Map<String, AtomicInteger> topicGenerations = new HashMap<>();

    public void update(TopicMetadataResponse metadataResponse) {
        clusterNodes = stream(metadataResponse.getBrokers()).collect(toMap(TopicMetadataBroker::getNodeId, identity()));

        Map<String, List<PartitionMetadata>> partitionsByTopic = new HashMap<>();
        Map<String, TopicMetadataBroker> topicLeaders = new HashMap<>();

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

    public void initTopicLog(String topic, int defaultGeneration) {
        topicGenerations.put(topic, new AtomicInteger(defaultGeneration));
    }

    public Integer generationForTopic(String topic) {
        return topicGenerations.get(topic).get();
    }

    public Optional<TopicMetadataBroker> leaderFor(String topic) {
        return Optional.ofNullable(topicLeaders.get(topic));
    }

    public List<Integer> brokersForTopic(String topic) {
        return partitionsByTopic.getOrDefault(topic, emptyList())
                .stream()
                .flatMap(partitionMetadata -> stream(partitionMetadata.getReplicas()))
                .collect(Collectors.toList());
    }

    public Optional<TopicMetadataBroker> brokerForId(Integer nodeId) {
        return Optional.ofNullable(clusterNodes.get(nodeId));
    }

    public Optional<TopicMetadataBroker> randomBroker() {
        List<TopicMetadataBroker> nodes = new ArrayList<>(clusterNodes.values());

        if (nodes.isEmpty()) {
            return Optional.empty();
        }

        Collections.shuffle(nodes);
        return Optional.ofNullable(nodes.iterator().next());
    }

    public GroupCoordinatorBroker computeCoordinatorIfAbsent(String topic,
            Supplier<GroupCoordinatorBroker> coordinatorSupplier) {
        return groupCoordinators.computeIfAbsent(topic, s -> coordinatorSupplier.get());
    }

    public void checkGroupJoined(String groupId, Supplier<String> memberIdSupplier) {
        groupMemberIds.computeIfAbsent(groupId, s -> memberIdSupplier.get());
    }

    public Integer partitionForTopic(String topic) {
        List<PartitionMetadata> partitionMetadatas = partitionsByTopic.getOrDefault(topic, emptyList());
        return partitionMetadatas.stream().findAny().map(PartitionMetadata::getPartitionId).orElse(0);
    }

    public Long offsetForTopic(String topic) {
        return 0L;
    }

    public int availableBrokers() {
        return clusterNodes.size();
    }
}
