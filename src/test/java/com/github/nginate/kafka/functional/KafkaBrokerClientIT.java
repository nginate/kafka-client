package com.github.nginate.kafka.functional;

import com.github.nginate.kafka.core.KafkaBrokerClient;
import com.github.nginate.kafka.protocol.messages.dto.Broker;
import com.github.nginate.kafka.protocol.messages.dto.PartitionStateInfoWrapper;
import com.github.nginate.kafka.protocol.messages.dto.TopicAndPartition;
import com.github.nginate.kafka.protocol.messages.request.*;
import com.github.nginate.kafka.protocol.messages.response.*;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.github.nginate.commons.lang.await.Await.waitUntil;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class KafkaBrokerClientIT extends AbstractFunctionalTest {

    private KafkaBrokerClient client;

    @BeforeClass(dependsOnMethods = "initDockerContainer")
    public void prepareClient() throws Exception {
        client = new KafkaBrokerClient(getKafkaHost(), getKafkaPort());

        waitUntil(10000, 1000, client::isConnectionAlive);

        log.info("Connected to Kafka broker");

        // waiting for broker registration in container
        waitUntil(10000, 1000, () -> !getZkClient().getChildren("/brokers/ids").isEmpty());
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClient() throws Exception {
        client.close();
    }

    @Test
    public void testProduceRequest() throws Exception {
        ProduceRequest request = ProduceRequest.builder()
                .topicProduceData(new ProduceRequest.TopicProduceData[0])
                .build();
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
        OffsetRequest request = OffsetRequest.builder()
                .topicData(new OffsetRequest.OffsetRequestTopicData[0])
                .build();
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
        LeaderAndIsrRequest request = LeaderAndIsrRequest.builder()
                .partitionStates(new LeaderAndIsrRequest.LeaderAndIsrRequestPartitionState[0])
                .liveLeaders(new Broker[0])
                .build();
        LeaderAndIsrResponse response = await(client.leaderAndIsr(request));

        assertThat(response).isNotNull();
    }

    @Test
    public void testStopReplicaRequest() throws Exception {
        StopReplicaRequest request = StopReplicaRequest.builder()
                .deletePartitions(false)
                .topicsAndPartitions(new TopicAndPartition[0])
                .build();
        StopReplicaResponse response = await(client.stopReplica(request));

        assertThat(response).isNotNull();
    }

    @Test
    public void testUpdateMetadataRequest() throws Exception {
        UpdateMetadataRequest request = UpdateMetadataRequest.builder()
                .partitionStates(new PartitionStateInfoWrapper[0])
                .liveBrokers(new UpdateMetadataRequest.UpdateMetadataBroker[0])
                .build();
        UpdateMetadataResponse response = await(client.updateMetadata(request));

        assertThat(response).isNotNull();
    }

    @Test
    public void testControlledShutdownRequest() throws Exception {
        ControlledShutdownRequest request = ControlledShutdownRequest.builder()
                .brokerId(10)
                .build();
        ControlledShutdownResponse response = await(client.controlledShutdown(request));

        assertThat(response).isNotNull();
    }

    @Test
    public void testOffsetCommitRequest() throws Exception {
        OffsetCommitRequest request = OffsetCommitRequest.builder()
                .consumerGroupId("")
                .consumerId("")
                .topicData(new OffsetCommitRequest.OffsetCommitRequestTopicData[0])
                .build();
        OffsetCommitResponse response = await(client.commitOffset(request));

        assertThat(response).isNotNull();
    }

    @Test
    public void testOffsetFetchRequest() throws Exception {
        OffsetFetchRequest request = OffsetFetchRequest.builder()
                .consumerGroup("")
                .topicData(new OffsetFetchRequest.OffsetFetchRequestTopicData[0])
                .build();
        OffsetFetchResponse response = await(client.fetchOffset(request));

        assertThat(response).isNotNull();
    }

    @Test
    public void testGroupCoordinatorRequest() throws Exception {
        GroupCoordinatorResponse response = await(client.getGroupCoordinator(""));
        assertThat(response).isNotNull();
    }

    @Test
    public void testDescribeGroupsRequest() throws Exception {
        DescribeGroupsResponse response = await(client.describeGroups());
        assertThat(response).isNotNull();
    }

    @Test
    public void testJoinGroupRequest() throws Exception {
        JoinGroupRequest request = JoinGroupRequest.builder()
                .groupId("")
                .memberId("")
                .protocolType("")
                .groupProtocols(new JoinGroupRequest.GroupProtocols[0])
                .build();

        JoinGroupResponse response = await(client.joinGroup(request));

        assertThat(response).isNotNull();
    }

    @Test
    public void testSASLHanshakeRequest() throws Exception {
        SASLHanshakeRequest request = SASLHanshakeRequest.builder()
                .mechanism("")
                .build();
        SASLHanshakeResponse response = await(client.saslHandshake(request));

        assertThat(response).isNotNull();
    }

    @Test
    public void testApiVersionsRequest() throws Exception {
        ApiVersionsResponse response = await(client.apiVersions());

        assertThat(response).isNotNull();
    }

    @Override
    protected String getKafkaBrokerVersion() {
        return "0.10";
    }
}
