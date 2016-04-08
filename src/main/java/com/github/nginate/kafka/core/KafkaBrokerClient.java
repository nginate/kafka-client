package com.github.nginate.kafka.core;

import com.github.nginate.kafka.exceptions.CommunicationException;
import com.github.nginate.kafka.network.client.BinaryMessageMetadata;
import com.github.nginate.kafka.network.client.BinaryTcpClient;
import com.github.nginate.kafka.network.client.BinaryTcpClientConfig;
import com.github.nginate.kafka.protocol.ApiKey;
import com.github.nginate.kafka.protocol.KafkaSerializer;
import com.github.nginate.kafka.protocol.messages.Request;
import com.github.nginate.kafka.protocol.messages.Response;
import com.github.nginate.kafka.protocol.messages.request.*;
import com.github.nginate.kafka.protocol.messages.response.*;

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

    public CompletableFuture<TopicMetadataResponse> topicMetadata(String... topicNames) {
        TopicMetadataRequest request = TopicMetadataRequest.builder().topic(topicNames).build();
        return topicMetadata(request);
    }

    public CompletableFuture<TopicMetadataResponse> topicMetadata(TopicMetadataRequest request) {
        return sendAndReceive(request, TopicMetadataResponse.class);
    }

    public CompletableFuture<DescribeGroupsResponse> describeGroups(String... groupIds) {
        DescribeGroupsRequest request = DescribeGroupsRequest.builder().groupIds(groupIds).build();
        return sendAndReceive(request, DescribeGroupsResponse.class);
    }

    public CompletableFuture<FetchResponse> fetch(FetchRequest request) {
        return sendAndReceive(request, FetchResponse.class);
    }

    public CompletableFuture<GroupCoordinatorResponse> getGroupCoordinator(String groupId) {
        GroupCoordinatorRequest request = GroupCoordinatorRequest.builder().groupId(groupId).build();
        return sendAndReceive(request, GroupCoordinatorResponse.class);
    }

    public CompletableFuture<HeartbeatResponse> checkHeartbeat(HeartbeatRequest request) {
        return sendAndReceive(request, HeartbeatResponse.class);
    }

    public CompletableFuture<JoinGroupResponse> joinGroup(JoinGroupRequest request) {
        return sendAndReceive(request, JoinGroupResponse.class);
    }

    public CompletableFuture<LeaveGroupResponse> leaveGroup(LeaveGroupRequest request) {
        return sendAndReceive(request, LeaveGroupResponse.class);
    }

    public CompletableFuture<ListGroupsResponse> listGroups() {
        ListGroupsRequest request = ListGroupsRequest.builder().build();
        return sendAndReceive(request, ListGroupsResponse.class);
    }

    public CompletableFuture<OffsetCommitResponse> commitOffset(OffsetCommitRequest request) {
        return sendAndReceive(request, OffsetCommitResponse.class);
    }

    public CompletableFuture<OffsetFetchResponse> fetchOffset(OffsetFetchRequest request) {
        return sendAndReceive(request, OffsetFetchResponse.class);
    }

    public CompletableFuture<OffsetResponse> getOffset(OffsetRequest request) {
        return sendAndReceive(request, OffsetResponse.class);
    }

    public CompletableFuture<ProduceResponse> produce(ProduceRequest request) {
        return sendAndReceive(request, ProduceResponse.class);
    }

    public CompletableFuture<SyncGroupResponse> syncGroup(SyncGroupRequest request) {
        return sendAndReceive(request, SyncGroupResponse.class);
    }

    public CompletableFuture<LeaderAndIsrResponse> leaderAndIsr(LeaderAndIsrRequest request) {
        return sendAndReceive(request, LeaderAndIsrResponse.class);
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
