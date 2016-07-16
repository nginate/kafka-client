package com.github.nginate.kafka.core;

import com.github.nginate.kafka.protocol.messages.response.GroupCoordinatorResponse.GroupCoordinatorBroker;
import com.github.nginate.kafka.protocol.messages.response.TopicMetadataResponse;
import com.github.nginate.kafka.protocol.messages.response.TopicMetadataResponse.TopicMetadata.PartitionMetadata;
import com.github.nginate.kafka.protocol.messages.response.TopicMetadataResponse.TopicMetadataBroker;
import lombok.Synchronized;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

class ClusterMetadata {
    @SuppressWarnings("unused")
    private final Object topicMonitor = new Object();

    private final Map<Integer, TopicMetadataBroker> clusterNodes = new HashMap<>();
    private final Map<String, List<PartitionMetadata>> partitionsByTopic = new HashMap<>();
    private final Map<String, TopicMetadataBroker> topicLeaders = new HashMap<>();
    //TODO try to remember the purpose of this
    private final Map<String, String> groupMemberIds = new ConcurrentHashMap<>();

    /**
     * Utility field to store topic names used by this client only. Should be filled by subscribe/publish requests
     */
    private final Set<String> topics = new CopyOnWriteArraySet<>();

    private final Map<String, GroupCoordinatorBroker> groupCoordinators = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<TopicMetadataBroker>> topicLeaderWatchers = new ConcurrentHashMap<>();

    @Synchronized
    void update(TopicMetadataResponse metadataResponse) {
        cleanCache();

        Map<Integer, TopicMetadataBroker> updatedClusterNodes = stream(metadataResponse.getBrokers())
                .collect(toMap(TopicMetadataBroker::getNodeId, identity()));

        Map<String, List<PartitionMetadata>> updatedPartitionsByTopic = new HashMap<>();
        Map<String, TopicMetadataBroker> updatedTopicLeaders = new HashMap<>();

        stream(metadataResponse.getTopicMetadata()).forEach(metadata -> {
            List<PartitionMetadata> partitionData = unmodifiableList(asList(metadata.getPartitionMetadata()));
            updatedPartitionsByTopic.put(metadata.getTopicName(), partitionData);

            partitionData.stream()
                    .filter(partitionMetadata -> partitionMetadata.getLeader() >= 0)
                    .findAny().ifPresent(partitionMetadata -> {
                TopicMetadataBroker leader = updatedClusterNodes.get(partitionMetadata.getLeader());
                updatedTopicLeaders.put(metadata.getTopicName(), leader);
                CompletableFuture<TopicMetadataBroker> future = topicLeaderWatchers.remove(metadata.getTopicName());
                if (future != null) {
                    future.complete(leader);
                }
            });
        });

        clusterNodes.putAll(updatedClusterNodes);
        partitionsByTopic.putAll(updatedPartitionsByTopic);
        topicLeaders.putAll(updatedTopicLeaders);
    }

    private void cleanCache() {
        clusterNodes.clear();
        partitionsByTopic.clear();
        topicLeaders.clear();
        groupCoordinators.clear();
    }

    @Synchronized
    public CompletableFuture<TopicMetadataBroker> leaderFor(String topic) {
        return topicLeaderWatchers.computeIfAbsent(topic, k -> new CompletableFuture<>());
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

    public boolean topicExists(String topic) {
        return partitionsByTopic.containsKey(topic);
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

    @Synchronized("topicMonitor")
    public void addTopic(String topic) {
        topics.add(topic);
    }

    @Synchronized("topicMonitor")
    public Set<String> getTopics() {
        return topics;
    }
}
