package com.github.nginate.kafka.core;

import com.github.nginate.kafka.exceptions.CommunicationException;
import com.github.nginate.kafka.network.client.BinaryMessageMetadata;
import com.github.nginate.kafka.network.client.BinaryTcpClient;
import com.github.nginate.kafka.network.client.BinaryTcpClientConfig;
import com.github.nginate.kafka.protocol.ApiKey;
import com.github.nginate.kafka.protocol.KafkaSerializer;
import com.github.nginate.kafka.protocol.messages.Request;
import com.github.nginate.kafka.protocol.messages.Response;
import com.github.nginate.kafka.protocol.messages.request.DescribeGroupsRequest;
import com.github.nginate.kafka.protocol.messages.request.TopicMetadataRequest;
import com.github.nginate.kafka.protocol.messages.response.DescribeGroupsResponse;
import com.github.nginate.kafka.protocol.messages.response.MetadataResponse;

import java.io.Closeable;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class KafkaBrokerClient implements Closeable {
    private final BinaryTcpClient binaryTcpClient;
    private final AtomicInteger correlationIdCounter = new AtomicInteger();
    private final String clientId = UUID.randomUUID().toString();

    public KafkaBrokerClient(String host, int port) {
        BinaryTcpClientConfig config = BinaryTcpClientConfig.custom()
                .serializer(new KafkaSerializer())
                .host(host)
                .port(port)
                .build();
        binaryTcpClient = new BinaryTcpClient(config);
    }

    public void connect() {
        binaryTcpClient.connect();
    }

    public CompletableFuture<MetadataResponse> topicMetadata(String... topicNames) {
        TopicMetadataRequest request = TopicMetadataRequest.builder().topic(topicNames).build();
        return sendAndReceive(request, MetadataResponse.class);
    }

    public CompletableFuture<DescribeGroupsResponse> describeGroups(String... groupIds) {
        DescribeGroupsRequest request = DescribeGroupsRequest.builder().groupIds(groupIds).build();
        return sendAndReceive(request, DescribeGroupsResponse.class);
    }

    private <T> CompletableFuture<T> sendAndReceive(Object payload, Class<T> responseClass)
            throws CommunicationException {
        short apiKey = payload.getClass().getAnnotation(ApiKey.class).value().getId();
        Request request = Request.builder()
                .apiVersion(apiKey)
                .clientId(clientId)
                .correlationId(correlationIdCounter.incrementAndGet())
                .message(payload)
                .build();
        return binaryTcpClient.request(request, Response.class, new BinaryMessageMetadata(responseClass))
                .thenApply(response -> responseClass.cast(response.getMessage()));
    }

    @Override
    public void close() {
        binaryTcpClient.close();
    }
}
