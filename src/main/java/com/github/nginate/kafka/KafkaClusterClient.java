package com.github.nginate.kafka;

import com.github.nginate.kafka.core.ClusterMetadata;
import com.github.nginate.kafka.dto.Partition;
import com.github.nginate.kafka.protocol.messages.Request;
import com.github.nginate.kafka.protocol.messages.Response;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

public class KafkaClusterClient implements Closeable {

    public InetAddress getLeader(Partition partition) {
        return null;
    }

    public List<Partition> getPartitions(List<String> topics) {
        return null;
    }

    public Partition getPartition(String topic) {
        return null;
    }

    public ClusterMetadata getTopicMetadata(String topic) {
        return null;
    }

    public <RS extends Response, RQ extends Request> RS request(RQ request) {
        return null;
    }

    @Override
    public void close() throws IOException {

    }
}
