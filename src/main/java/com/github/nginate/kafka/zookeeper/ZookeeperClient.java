package com.github.nginate.kafka.zookeeper;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.serialize.ZkSerializer;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.nginate.kafka.util.JsonUtil.fromJson;

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
}
