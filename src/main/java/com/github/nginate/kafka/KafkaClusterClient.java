package com.github.nginate.kafka;

import com.github.nginate.kafka.core.ClusterMetadata;
import com.github.nginate.kafka.core.KafkaBrokerClient;
import com.github.nginate.kafka.dto.Partition;
import com.github.nginate.kafka.exceptions.CommunicationException;
import com.github.nginate.kafka.protocol.messages.Request;
import com.github.nginate.kafka.protocol.messages.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
public class KafkaClusterClient implements Closeable {

    private final String topic;

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

    public <T extends Response> CompletableFuture<T> sendAndReceive(Request request, Class<T> responseClass)
            throws CommunicationException {
        return getClientForCurrentTopicLeader().sendAndReceive(request, responseClass);
    }

    @Override
    public void close() throws IOException {

    }

    private KafkaBrokerClient getClientForCurrentTopicLeader() {
        return null;
    }
}
