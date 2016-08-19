package com.github.nginate.kafka.core;

import com.github.nginate.kafka.protocol.messages.dto.Broker;
import com.github.nginate.kafka.protocol.messages.response.JoinGroupResponse;
import com.github.nginate.kafka.protocol.messages.response.ListGroupsResponse;
import com.github.nginate.kafka.protocol.messages.response.TopicMetadataResponse;
import com.github.nginate.kafka.protocol.messages.response.TopicMetadataResponse.TopicMetadata.PartitionMetadata;
import lombok.Synchronized;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

class ClusterMetadata {
    @SuppressWarnings("unused")
    private final Object topicMonitor = new Object();
    @SuppressWarnings("unused")
    private final Object groupMonitor = new Object();

    private final Map<Integer, Broker> clusterNodes = new HashMap<>();
    private final Map<String, List<PartitionMetadata>> partitionsByTopic = new HashMap<>();
    private final Map<String, Broker> topicLeaders = new HashMap<>();
    private final Map<String, JoinGroupResponse> memberData = new ConcurrentHashMap<>();

    /**
     * Utility field to store topic names used by this client only. Should be filled by subscribe/publish requests
     */
    private final Set<String> topics = new CopyOnWriteArraySet<>();

    private final Map<String, CompletableFuture<Broker>> topicLeaderWatchers = new ConcurrentHashMap<>();
    private Map<String, List<Broker>> groupBrokers = new HashMap<>();

    @Synchronized
    void update(TopicMetadataResponse metadataResponse) {
        cleanCache();

        Map<Integer, Broker> updatedClusterNodes = stream(metadataResponse.getBrokers())
                .collect(toMap(Broker::getNodeId, identity()));

        Map<String, List<PartitionMetadata>> updatedPartitionsByTopic = new HashMap<>();
        Map<String, Broker> updatedTopicLeaders = new HashMap<>();

        stream(metadataResponse.getTopicMetadata()).forEach(metadata -> {
            List<PartitionMetadata> partitionData = unmodifiableList(asList(metadata.getPartitionMetadata()));
            updatedPartitionsByTopic.put(metadata.getTopicName(), partitionData);

            partitionData.stream()
                    .filter(partitionMetadata -> partitionMetadata.getLeader() >= 0)
                    .findAny().ifPresent(partitionMetadata -> {
                Broker leader = updatedClusterNodes.get(partitionMetadata.getLeader());
                updatedTopicLeaders.put(metadata.getTopicName(), leader);
                CompletableFuture<Broker> future = topicLeaderWatchers.remove(metadata.getTopicName());
                if (future != null) {
                    future.complete(leader);
                }
            });
        });

        clusterNodes.putAll(updatedClusterNodes);
        partitionsByTopic.putAll(updatedPartitionsByTopic);
        topicLeaders.putAll(updatedTopicLeaders);
    }

    @Synchronized("groupMonitor")
    private void cleanCache() {
        clusterNodes.clear();
        partitionsByTopic.clear();
        topicLeaders.clear();
    }

    @Synchronized
    public CompletableFuture<Broker> leaderFor(String topic) {
        return topicLeaderWatchers.computeIfAbsent(topic, k -> new CompletableFuture<>());
    }

    @Synchronized
    public List<Broker> brokersForTopic(String topic) {
        return partitionsByTopic.getOrDefault(topic, emptyList())
                .stream()
                .flatMap(partitionMetadata -> stream(partitionMetadata.getReplicas()))
                .map(clusterNodes::get)
                .collect(Collectors.toList());
    }

    public Optional<Broker> brokerForId(Integer nodeId) {
        return Optional.ofNullable(clusterNodes.get(nodeId));
    }

    public Collection<Broker> clusterNodes() {
        return clusterNodes.values();
    }

    public Optional<Broker> randomBroker() {
        List<Broker> nodes = new ArrayList<>(clusterNodes.values());

        if (nodes.isEmpty()) {
            return Optional.empty();
        }

        Collections.shuffle(nodes);
        return Optional.ofNullable(nodes.iterator().next());
    }

    @Synchronized("groupMonitor")
    public Broker coordinator(String groupId) {
        return Optional.ofNullable(groupBrokers.get(groupId)).map(list -> list.get(0)).orElse(null);
    }

    @Synchronized("groupMonitor")
    public String getMemberId(String groupId) {
        return Optional.ofNullable(memberData.get(groupId)).map(JoinGroupResponse::getMemberId).orElse(null);
    }

    @Synchronized("groupMonitor")
    public Integer generation(String groupId) {
        return Optional.ofNullable(memberData.get(groupId)).map(JoinGroupResponse::getGenerationId).orElse(null);
    }

    @Synchronized("groupMonitor")
    public void setMemberData(String consumerGroupId, JoinGroupResponse response) {
        memberData.put(consumerGroupId, response);
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

    @Synchronized("groupMonitor")
    public void updateGroups(Map<Broker, ListGroupsResponse.Group[]> groups) {
        groupBrokers.clear();
        groups.entrySet().stream()
                .flatMap(entry -> stream(entry.getValue())
                        .map(ListGroupsResponse.Group::getGroupId)
                        .collect(toMap(identity(), o -> entry.getKey())).entrySet().stream())
                .collect(groupingBy(Map.Entry::getKey))
                .forEach((group, list) -> {
                    List<Broker> brokers = list.stream().map(Map.Entry::getValue).collect(Collectors.toList());
                    groupBrokers.put(group, brokers);
                });
    }
}
