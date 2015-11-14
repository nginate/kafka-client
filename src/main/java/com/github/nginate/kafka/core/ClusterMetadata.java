package com.github.nginate.kafka.core;

import com.github.nginate.kafka.protocol.KafkaNode;
import com.github.nginate.kafka.protocol.Partition;
import com.github.nginate.kafka.protocol.TopicPartition;
import lombok.Synchronized;

import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.github.nginate.kafka.util.CollectionUtils.unmodifiedOptionalCopy;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toMap;

@ThreadSafe
public class ClusterMetadata {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final List<KafkaNode> clusterNodes = new ArrayList<>();
    private final Map<TopicPartition, Partition> partitionsByTopicPartition = new HashMap<>();
    private final Map<String, List<Partition>> partitionsByTopic = new HashMap<>();
    private final Map<String, List<Partition>> availablePartitionsByTopic = new HashMap<>();
    private final Map<Integer, List<Partition>> partitionsByNodeId = new HashMap<>();

    @Synchronized
    public void update(List<KafkaNode> nodes, List<Partition> partitions) {
        cleanAll();
        // make a randomized copy of the nodes
        List<KafkaNode> copy = new ArrayList<>(nodes);
        Collections.shuffle(copy);
        clusterNodes.addAll(copy);

        // index the partitions by topic/partition for quick lookup
        partitionsByTopicPartition.putAll(
                partitions.stream().collect(toMap(partition ->
                        new TopicPartition(partition.getPartition(), partition.getTopic()), partition -> partition))
        );

        // index the partitions by topic and node respectively, and make the lists
        // unmodifiable so we can hand them out in user-facing apis without risk
        // of the client modifying the contents
        Map<Integer, List<Partition>> partitionsForNode = clusterNodes.stream()
                .collect(toMap(KafkaNode::getId, kafkaNode -> new ArrayList<>()));

        Map<String, List<Partition>> partitionsForTopic = new HashMap<>();
        Map<String, List<Partition>> partitionsWithLeader = new HashMap<>();
        partitions.forEach(partition -> {
            partitionsForTopic.computeIfAbsent(partition.getTopic(), s -> new ArrayList<>());
            partitionsWithLeader.computeIfAbsent(partition.getTopic(), s -> new ArrayList<>());
            partitionsForTopic.get(partition.getTopic()).add(partition);

            Optional.ofNullable(partition.getLeader()).ifPresent(kafkaNode -> {
                //Originally contained npe check for existing node
                partitionsForNode.get(kafkaNode.getId()).add(partition);
                partitionsWithLeader.get(partition.getTopic()).add(partition);
            });
        });

        partitionsByTopic.putAll(partitionsForTopic);
        availablePartitionsByTopic.putAll(partitionsWithLeader);
        partitionsByNodeId.putAll(partitionsForNode);
    }

    @Synchronized
    public List<KafkaNode> getClusterNodes() {
        return unmodifiableList(clusterNodes);
    }

    @Synchronized
    public Set<String> getTopics() {
        return partitionsByTopic.keySet();
    }

    @Synchronized
    public Optional<KafkaNode> leaderFor(TopicPartition topicPartition) {
        return Optional.ofNullable(partitionsByTopicPartition.get(topicPartition).getLeader());
    }

    @Synchronized
    public Optional<Partition> partition(TopicPartition topicPartition) {
        return Optional.ofNullable(partitionsByTopicPartition.get(topicPartition));
    }

    @Synchronized
    public Optional<List<Partition>> getTopicPartitions(String topic) {
        return unmodifiedOptionalCopy(partitionsByTopic.get(topic));
    }

    @Synchronized
    public Optional<List<Partition>> getNodePartitions(int nodeId) {
        return unmodifiedOptionalCopy(partitionsByNodeId.get(nodeId));
    }

    @Synchronized
    public Optional<List<Partition>> getTopicPartitionsWithLeader(String topic) {
        return unmodifiedOptionalCopy(availablePartitionsByTopic.get(topic));
    }

    @Synchronized
    public void clear() {
        cleanAll();
    }

    private void cleanAll() {
        clusterNodes.clear();
        partitionsByTopicPartition.clear();
        partitionsByTopic.clear();
        availablePartitionsByTopic.clear();
        partitionsByNodeId.clear();
    }
}
