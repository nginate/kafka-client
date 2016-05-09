package com.github.nginate.kafka.functional;

import com.github.nginate.kafka.protocol.messages.request.*;
import com.github.nginate.kafka.protocol.messages.response.*;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class KafkaBrokerClient_0_8_IT extends AbstractKafkaBrokerClientTest {

    @Test
    public void testProduceRequest() throws Exception {
        ProduceRequest request = ProduceRequest.builder().build();
        ProduceResponse response = await(client.produce(request));

        assertThat(response).isNotNull();
    }

    @Test
    public void testFetchRequest() throws Exception {
        FetchRequest request = FetchRequest.builder().maxWaitTime(100).build();
        FetchResponse response = await(client.fetch(request));

        assertThat(response).isNotNull();
    }

    @Test
    public void testOffsetsRequest() throws Exception {
        OffsetRequest request = OffsetRequest.builder().build();
        OffsetResponse response = await(client.getOffset(request));

        assertThat(response).isNotNull();
    }

    @Test
    public void testMetadataRequest() throws Exception {
        TopicMetadataResponse response = await(client.topicMetadata());

        assertThat(response).isNotNull();
    }

    @Test
    public void testLeaderAndIsrRequest() throws Exception {
        LeaderAndIsrRequest request = LeaderAndIsrRequest.builder().build();
        LeaderAndIsrResponse response = await(client.leaderAndIsr(request));

        assertThat(response).isNotNull();
    }

    @Test
    public void testStopReplicaRequest() throws Exception {
        StopReplicaRequest request = StopReplicaRequest.builder().deletePartitions((byte) 0).build();
        StopReplicaResponse response = await(client.stopReplica(request));

        assertThat(response).isNotNull();
    }

    @Test
    public void testUpdateMetadataRequest() throws Exception {
        UpdateMetadataRequest request = UpdateMetadataRequest.builder().build();
        UpdateMetadataResponse response = await(client.updateMetadata(request));

        assertThat(response).isNotNull();
    }

    @Test
    public void testControlledShutdownRequest() throws Exception {
        ControlledShutdownRequest request = ControlledShutdownRequest.builder().build();
        ControlledShutdownResponse response = await(client.controlledShutdown(request));

        assertThat(response).isNotNull();
    }

    @Test
    public void testOffsetCommitRequest() throws Exception {
        OffsetCommitRequest request = OffsetCommitRequest.builder().build();
        OffsetCommitResponse response = await(client.commitOffset(request));

        assertThat(response).isNotNull();
    }

    @Test
    public void testOffsetFetchRequest() throws Exception {
        OffsetFetchRequest request = OffsetFetchRequest.builder().build();
        OffsetFetchResponse response = await(client.fetchOffset(request));

        assertThat(response).isNotNull();
    }

    @Test
    public void testGroupCoordinatorRequest() throws Exception {
        GroupCoordinatorResponse response = await(client.getGroupCoordinator(""));
        assertThat(response).isNotNull();
    }

    @Override
    protected String getKafkaBrokerVersion() {
        return "0.8";
    }
}
