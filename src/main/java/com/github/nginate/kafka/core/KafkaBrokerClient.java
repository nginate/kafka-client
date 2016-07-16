package com.github.nginate.kafka.core;

import com.github.nginate.kafka.exceptions.CommunicationException;
import com.github.nginate.kafka.network.client.BinaryMessageMetadata;
import com.github.nginate.kafka.network.client.BinaryTcpClient;
import com.github.nginate.kafka.network.client.BinaryTcpClientConfig;
import com.github.nginate.kafka.protocol.messages.Request;
import com.github.nginate.kafka.protocol.messages.Response;
import com.github.nginate.kafka.protocol.messages.request.*;
import com.github.nginate.kafka.protocol.messages.response.*;
import com.github.nginate.kafka.serialization.ApiVersion;
import com.github.nginate.kafka.serialization.BinaryMessageSerializerImpl;
import com.google.common.base.Throwables;
import org.apache.commons.lang3.ArrayUtils;

import java.io.Closeable;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class KafkaBrokerClient implements Closeable {
    private final BinaryTcpClient binaryTcpClient;
    private final AtomicInteger correlationIdCounter = new AtomicInteger();
    private final String clientId = UUID.randomUUID().toString();

    public KafkaBrokerClient(String host, int port) {
        BinaryTcpClientConfig config = BinaryTcpClientConfig.custom()
                .serializer(new BinaryMessageSerializerImpl())
                .host(host)
                .port(port)
                .build();
        binaryTcpClient = new BinaryTcpClient(config);
    }

    public boolean isConnectionAlive() {
        return binaryTcpClient.isConnectionAlive();
    }

    public CompletableFuture<TopicMetadataResponse> topicMetadata(String... topicNames) {
        String[] requestTopics = ArrayUtils.isEmpty(topicNames) ? null : topicNames;
        TopicMetadataRequest request = TopicMetadataRequest.builder().topics(requestTopics).build();
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
        return sendAndReceive(request, LeaderAndIsrResponse.class); //TODO transform error codes into exceptions
    }

    public CompletableFuture<StopReplicaResponse> stopReplica(StopReplicaRequest request) {
        return sendAndReceive(request, StopReplicaResponse.class); //TODO transform error codes into exceptions
    }

    public CompletableFuture<UpdateMetadataResponse> updateMetadata(UpdateMetadataRequest request) {
        return sendAndReceive(request, UpdateMetadataResponse.class); //TODO transform error codes into exceptions
    }

    public CompletableFuture<ControlledShutdownResponse> controlledShutdown(ControlledShutdownRequest request) {
        return sendAndReceive(request, ControlledShutdownResponse.class);  //TODO transform error codes into exceptions
    }

    public CompletableFuture<SASLHanshakeResponse> saslHandshake(SASLHanshakeRequest request) {
        return sendAndReceive(request, SASLHanshakeResponse.class);  //TODO transform error codes into exceptions
    }

    public CompletableFuture<ApiVersionsResponse> apiVersions() {
        ApiVersionsRequest request = ApiVersionsRequest.builder().build();
        return apiVersions(request);
    }

    public CompletableFuture<ApiVersionsResponse> apiVersions(ApiVersionsRequest request) {
        return sendAndReceive(request, ApiVersionsResponse.class);  //TODO transform error codes into exceptions
    }

    private <T> CompletableFuture<T> sendAndReceive(Object payload, Class<T> responseClass)
            throws CommunicationException {
        Short apiVersion = Optional.ofNullable(payload.getClass().getAnnotation(ApiVersion.class))
                .map(ApiVersion::value)
                .orElse((short) 0);
        Request request = Request.builder()
                .apiVersion(apiVersion)
                .clientId(clientId)
                .correlationId(correlationIdCounter.incrementAndGet())
                .message(payload)
                .build();
        return binaryTcpClient.request(request, Response.class, new BinaryMessageMetadata(responseClass))
                .thenApply(response -> responseClass.cast(response.getMessage()));
    }

    @Override
    public void close() {
        try {
            binaryTcpClient.close().get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw Throwables.propagate(e);
        }
    }
}
