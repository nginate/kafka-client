package com.github.nginate.kafka.zookeeper;

import com.github.nginate.kafka.util.JsonUtil;
import com.github.nginate.kafka.zookeeper.dto.TopicConfiguration;
import com.github.nginate.kafka.zookeeper.dto.TopicReplicaAssignment;
import com.github.nginate.kafka.zookeeper.dto.ZkBrokerInfo;
import com.github.nginate.kafka.zookeeper.exception.ZookeeperValidationException;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.serialize.ZkSerializer;

import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.nginate.kafka.util.JsonUtil.fromJson;
import static com.github.nginate.kafka.util.StringUtils.format;
import static java.util.stream.Collectors.*;
import static org.apache.commons.lang3.RandomUtils.nextInt;

public class ZookeeperClient {

    private static final String ConsumersPath = "/consumers";
    private static final String BrokerIdsPath = "/brokers/ids";
    private static final String BrokerTopicsPath = "/brokers/topics";
    private static final String TopicConfigPath = "/config/topics";
    private static final String TopicConfigChangesPath = "/config/changes";
    private static final String ControllerPath = "/controller";
    private static final String ControllerEpochPath = "/controller_epoch";
    private static final String ReassignPartitionsPath = "/admin/reassign_partitions";
    private static final String DeleteTopicsPath = "/admin/delete_topics";
    private static final String PreferredReplicaLeaderElectionPath = "/admin/preferred_replica_election";

    private final ZkClient zkClient;

    public ZookeeperClient(ZkClient zkClient) {
        this.zkClient = zkClient;
        this.zkClient.setZkSerializer(new ZkSerializer() {
            @Override
            public byte[] serialize(Object data) throws ZkMarshallingError {
                return data.toString().getBytes(Charset.forName("UTF-8"));
            }

            @Override
            public Object deserialize(byte[] bytes) throws ZkMarshallingError {
                return new String(bytes, Charset.forName("UTF-8"));
            }
        });
    }

    public List<ZkBrokerInfo> getClusterNodes() {
        return getChildrenParentMayNotExist(BrokerIdsPath)
                .stream()
                .map(Integer::parseInt)
                .map(this::getBrokerInfo)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * This API takes in a broker id, queries zookeeper for the broker metadata and returns the metadata for that broker
     * or throws an exception if the broker dies before the query to zookeeper finishes
     *
     * @param brokerId The broker id
     * @return An optional Broker object encapsulating the broker metadata
     */
    public Optional<ZkBrokerInfo> getBrokerInfo(int brokerId) {
        return readDataMaybeNull(BrokerIdsPath + "/" + brokerId).map(s -> {
            ZkBrokerInfo zkBrokerInfo = fromJson(s, ZkBrokerInfo.class);
            zkBrokerInfo.setBrokerId(brokerId);
            return zkBrokerInfo;
        });
    }

    public void createTopic(String topic, Integer partitions, Integer replicationFactor, Map<String, String> config) {
        List<ZkBrokerInfo> clusterNodes = getClusterNodes().stream()
                .sorted(Comparator.comparing(ZkBrokerInfo::getBrokerId))
                .collect(Collectors.toList());

        Validators.validateTopic(topic);
        Validators.validateTopicConfig(config);

        readDataMaybeNull(BrokerTopicsPath + "/" + topic).ifPresent(existingTopic -> {
            throw new ZookeeperValidationException(format("Topic {} already exists : {}", topic, existingTopic));
        });
        if (Validators.topicHasCollisionChars(topic)) {
            String normalizedTopic = topic.replace(".", "_");
            getChildrenParentMayNotExist(BrokerTopicsPath).stream()
                    .filter(retrievedTopic -> retrievedTopic.replace(".", "_").equals(normalizedTopic))
                    .findAny()
                    .ifPresent(existingTopic -> {
                        throw new ZookeeperValidationException(format("Topic {} collides with existing: {}",
                                topic, existingTopic));
                    });
        }

        Map<Integer, Set<Integer>> assignment = assignReplicasToBrokers(clusterNodes, partitions, replicationFactor);
        Set<Integer> uniquePartitionsSizes = assignment.values().stream().map(Set::size).collect(Collectors.toSet());
        if (uniquePartitionsSizes.size() != 1) {
            throw new ZookeeperValidationException("All partitions should have same size");
        }
        assignment.values().stream().map(Set::size).reduce((size1, size2) -> {
            if (!size1.equals(size2)) {
                throw new ZookeeperValidationException("Duplicate replica assignment found: " + assignment);
            }
            return size1;
        });

        TopicConfiguration topicConfiguration = TopicConfiguration.builder()
                .version(1).config(config)
                .build();
        writeData(TopicConfigPath + "/" + topic, JsonUtil.toJson(topicConfiguration));

        TopicReplicaAssignment replicaAssignment = TopicReplicaAssignment.builder()
                .version(1)
                .partitions(assignment)
                .build();
        writeData(BrokerTopicsPath + "/" + topic, JsonUtil.toJson(replicaAssignment));
    }

    private Map<Integer, Set<Integer>> assignReplicasToBrokers(List<ZkBrokerInfo> brokers, int partitions,
            int replicationFactor) {
        int replicaFactor = replicationFactor > brokers.size() ? brokers.size() : replicationFactor;
        brokers.forEach(zkBrokerInfo -> {
            String rack = Optional.ofNullable(zkBrokerInfo.getRack()).orElse("default");
            zkBrokerInfo.setRack(rack);
        });

        return assignReplicasToBrokersRackAware(brokers, partitions, replicaFactor, -1, -1);
    }

    private Map<Integer, Set<Integer>> assignReplicasToBrokersRackAware(List<ZkBrokerInfo> brokers, int partitions,
            int replicationFactor, int fixedStartIndex, int startPartitionId) {

        Map<Integer, String> brokerRackMap = brokers.stream()
                .collect(toMap(ZkBrokerInfo::getBrokerId, ZkBrokerInfo::getRack));

        int numRacks = new HashSet<>(brokerRackMap.values()).size();
        List<Integer> arrangedBrokerList = getRackAlternatedBrokerList(brokerRackMap);
        int numBrokers = arrangedBrokerList.size();

        Map<Integer, Set<Integer>> brokersForPartition = new HashMap<>();

        int startIndex = fixedStartIndex >= 0 ? fixedStartIndex : nextInt(0, arrangedBrokerList.size());
        int currentPartitionId = Math.max(0, startPartitionId);
        int nextReplicaShift = fixedStartIndex >= 0 ? fixedStartIndex : nextInt(0, arrangedBrokerList.size());

        for (int i = 0; i < partitions; i++) {
            if (currentPartitionId > 0 && (currentPartitionId % arrangedBrokerList.size() == 0)) {
                nextReplicaShift += 1;
            }

            int firstReplicaIndex = (currentPartitionId + startIndex) % arrangedBrokerList.size();
            int leader = arrangedBrokerList.get(firstReplicaIndex);

            Set<Integer> replicaBuffer = new HashSet<>();
            replicaBuffer.add(leader);

            Set<String> racksWithReplicas = new HashSet<>();
            racksWithReplicas.add(brokerRackMap.get(leader));

            Set<Integer> brokersWithReplicas = new HashSet<>();
            brokersWithReplicas.add(leader);

            int k = 0;
            for (int j = 0; j < replicationFactor - 1; j++) {

                boolean done = false;
                while (!done) {
                    int brokerId = arrangedBrokerList.get(
                            replicaIndex(firstReplicaIndex, nextReplicaShift * numRacks, k, arrangedBrokerList.size()));
                    String rack = brokerRackMap.get(brokerId);
                    // Skip this brokerId if
                    // 1. there is already a brokerId in the same rack that has assigned a replica AND there is one
                    // or more racks
                    //    that do not have any replica, or
                    // 2. the brokerId has already assigned a replica AND there is one or more brokers that do not
                    // have replica assigned
                    if ((!racksWithReplicas.contains(rack) || racksWithReplicas.size() == numRacks)
                            && (!brokersWithReplicas.contains(brokerId) || brokersWithReplicas.size() == numBrokers)) {
                        replicaBuffer.add(brokerId);
                        racksWithReplicas.add(rack);
                        brokersWithReplicas.add(brokerId);
                        done = true;
                    }
                    k += 1;
                }
            }
            brokersForPartition.put(currentPartitionId, replicaBuffer);
            currentPartitionId += 1;
        }
        return brokersForPartition;
    }

    /**
     * Given broker and rack information, returns a list of brokers alternated by the rack. Assume
     * this is the rack and its brokers:
     * <p>
     * rack1: 0, 1, 2
     * rack2: 3, 4, 5
     * rack3: 6, 7, 8
     * <p>
     * This API would return the list of 0, 3, 6, 1, 4, 7, 2, 5, 8
     * <p>
     * This is essential to make sure that the assignReplicasToBrokers API can use such list and
     * assign replicas to brokers in a simple round-robin fashion, while ensuring an even
     * distribution of leader and replica counts on each broker and that replicas are
     * distributed to all racks.
     */
    private List<Integer> getRackAlternatedBrokerList(Map<Integer, String> brokerRackMap) {
        Map<String, Iterator<Integer>> idsByRack = brokerRackMap.entrySet().stream()
                .collect(groupingBy(Map.Entry::getValue, mapping(Map.Entry::getKey, toSet())))
                .entrySet().stream()
                .collect(toMap(Map.Entry::getKey, entry -> entry.getValue().iterator()));

        final List<Integer> result = new ArrayList<>();

        while (result.size() < brokerRackMap.size()) {
            idsByRack.forEach((rack, iterator) -> {
                if (iterator.hasNext()) {
                    result.add(iterator.next());
                }
            });
        }
        return result;
    }

    private int replicaIndex(int firstReplicaIndex, int secondReplicaShift, int replicaIndex, int nBrokers) {
        int shift = 1 + (secondReplicaShift + replicaIndex) % (nBrokers - 1);
        return (firstReplicaIndex + shift) % nBrokers;
    }

    private List<String> getChildrenParentMayNotExist(String path) {
        try {
            return zkClient.getChildren(path);
        } catch (ZkNoNodeException e) {
            return Collections.emptyList();
        }
    }

    private Optional<String> readDataMaybeNull(String path) {
        try {
            return Optional.of(zkClient.readData(path));
        } catch (ZkNoNodeException e) {
            return Optional.empty();
        }
    }

    private void writeData(String path, Object data) {
        try {
            zkClient.writeData(path, data);
        } catch (ZkNoNodeException e) {
            zkClient.createPersistent(path, data);
        }
    }
}
