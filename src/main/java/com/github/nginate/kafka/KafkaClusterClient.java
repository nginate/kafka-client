package com.github.nginate.kafka;

import com.github.nginate.kafka.core.ClusterMetadata;
import com.github.nginate.kafka.protocol.Partition;

import java.io.Closeable;
import java.net.InetAddress;
import java.util.List;

public interface KafkaClusterClient extends Closeable {

    InetAddress getLeader(Partition partition);

    List<Partition> getPartitions(List<String> topics);

    Partition getPartition(String topic);

    ClusterMetadata getTopicMetadata(String topic);
}
