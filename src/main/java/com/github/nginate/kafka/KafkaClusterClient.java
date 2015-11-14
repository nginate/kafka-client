package com.github.nginate.kafka;

import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;

import java.io.Closeable;
import java.net.InetAddress;
import java.util.List;

public interface KafkaClusterClient extends Closeable {

    InetAddress getLeader(PartitionInfo partitionInfo);

    List<PartitionInfo> getPartitions(List<String> topics);

    PartitionInfo getPartition(String topic);

    Cluster getTopicMetadata(String topic);
}
