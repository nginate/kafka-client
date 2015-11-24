package com.github.nginate.kafka;

import com.github.nginate.kafka.core.ClusterMetadata;
import com.github.nginate.kafka.core.KafkaConnection;
import com.github.nginate.kafka.core.KafkaConnectionPool;
import com.github.nginate.kafka.dto.Partition;
import com.github.nginate.kafka.exceptions.CommunicationException;
import com.github.nginate.kafka.exceptions.ConnectionException;
import com.github.nginate.kafka.exceptions.KafkaTimeoutException;
import com.github.nginate.kafka.exceptions.SerializationException;
import com.github.nginate.kafka.protocol.Serializer;
import com.github.nginate.kafka.protocol.messages.Request;
import com.github.nginate.kafka.protocol.messages.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.github.nginate.kafka.util.StringUtils.format;

@Slf4j
@RequiredArgsConstructor
public class KafkaClusterClient implements Closeable {

    private final KafkaConnectionPool connectionPool;
    private final Serializer serializer;

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

    public <RS extends Response, RQ extends Request> RS request(RQ request, long timeout) throws KafkaTimeoutException {
        try {
            CompletableFuture<RS> future = new CompletableFuture<>();
            callInConnection(getAddress(), request, future);
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new KafkaTimeoutException(format("Request '{}' execution failed", request), e);
        }
    }

    @Override
    public void close() throws IOException {

    }

    private InetAddress getAddress() throws CommunicationException {
        try {
            return InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException e) {
            throw new CommunicationException("Unknown host", e);
        }
    }

    private <RS extends Response, RQ extends Request> void callInConnection(InetAddress address, RQ request,
                                                                                    CompletableFuture<RS> future) {
        KafkaConnection connection = null;
        try {
            connection = connectionPool.connect(address);
            byte[] rawResponse = connection.sendAndReceive(serializer.serialize(request));
            future.complete(serializer.deserialize(rawResponse));
        } catch (ConnectionException e) {
            log.error("Could not connect to {}", address, e);
            future.completeExceptionally(e);
        } catch (CommunicationException e) {
            log.error("Communication error with {}", address, e);
            future.completeExceptionally(e);
        } catch (SerializationException e) {
            log.error("Failed on serialization", e);
            future.completeExceptionally(e);
        } finally {
            connectionPool.releaseConnection(connection);
        }
    }
}
