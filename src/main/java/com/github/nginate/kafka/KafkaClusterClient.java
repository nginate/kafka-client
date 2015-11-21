package com.github.nginate.kafka;

import com.github.nginate.kafka.core.ClusterMetadata;
import com.github.nginate.kafka.dto.Partition;
import com.github.nginate.kafka.protocol.messages.Request;
import com.github.nginate.kafka.protocol.messages.Response;

import java.io.Closeable;
import java.net.InetAddress;
import java.util.List;

public interface KafkaClusterClient extends Closeable {

    InetAddress getLeader(Partition partition);

    List<Partition> getPartitions(List<String> topics);

    Partition getPartition(String topic);

    ClusterMetadata getTopicMetadata(String topic);

    <RS extends Response, RQ extends Request> RS request(RQ request);
}
