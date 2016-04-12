package com.github.nginate.kafka.functional;

import com.github.nginate.kafka.core.KafkaBrokerClient;
import com.github.nginate.kafka.protocol.messages.request.*;
import com.github.nginate.kafka.protocol.messages.response.*;
import kafka.cluster.Broker;
import kafka.utils.ZkUtils;
import kafka.utils.ZkUtils$;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.ZKUtil;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import scala.collection.Seq;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.github.nginate.kafka.util.WaitUtil.waitUntil;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class KafkaBrokerClientIT extends AbstractFunctionalTest {

    private KafkaBrokerClient client;

    @BeforeClass
    public void prepareClient() throws Exception {
        waitUntil(10000, 1000, () -> {
            try {
                client = new KafkaBrokerClient(getKafkaContainer().getIp(), getTestProperties().getKafkaPort());
                client.connect();
                log.info("Connected");
                return true;
            } catch (Exception e) {
                log.warn("Could not connect : {}", e.getMessage());
                return false;
            }
        });

        ZkUtils.setupCommonPaths(getZkClient());

        // waiting for broker registration in container
        waitUntil(10000, 1000, () -> {
            try
            {
                return !getZkClient().getChildren("/brokers/ids").isEmpty();
            }
            catch (Exception e)
            {
                log.warn("Could not retrieve broker list : {}", e.getMessage());
                return false;
            }
        });
    }

    @AfterClass
    public void tearDownClient() throws Exception {
        client.close();
    }

    @Test(enabled = false) // is not present in kafka 0.8.2
    public void testDescribeGroupsRequest() throws Exception {
        DescribeGroupsResponse response = await(client.describeGroups());
        assertThat(response).isNotNull();
    }

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

    private <T> T await(CompletableFuture<T> completableFuture)
            throws InterruptedException, ExecutionException, TimeoutException {
        return await(completableFuture, getTestProperties().getClientTimeout());
    }
    private <T> T await(CompletableFuture<T> completableFuture, int timeout)
            throws InterruptedException, ExecutionException, TimeoutException {
        return completableFuture.get(getTestProperties().getClientTimeout(), TimeUnit.MILLISECONDS);
    }

}
