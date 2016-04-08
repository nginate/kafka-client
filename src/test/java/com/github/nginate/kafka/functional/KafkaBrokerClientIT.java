package com.github.nginate.kafka.functional;

import com.github.nginate.kafka.core.KafkaBrokerClient;
import com.github.nginate.kafka.protocol.messages.request.FetchRequest;
import com.github.nginate.kafka.protocol.messages.request.LeaderAndIsrRequest;
import com.github.nginate.kafka.protocol.messages.request.LeaderAndIsrRequest.PartitionStateInfoWrapper;
import com.github.nginate.kafka.protocol.messages.request.LeaderAndIsrRequest.PartitionStateInfoWrapper.PartitionStateInfo;
import com.github.nginate.kafka.protocol.messages.request.OffsetRequest;
import com.github.nginate.kafka.protocol.messages.response.*;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
    }

    @AfterMethod
    public void tearDownClient() throws Exception {
        client.close();
    }

    @Test(enabled = false) // is not present in kafka 0.8.2
    public void testDescribeGroupsRequest() throws Exception {
        DescribeGroupsResponse response = await(client.describeGroups());
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

    private <T> T await(CompletableFuture<T> completableFuture)
            throws InterruptedException, ExecutionException, TimeoutException {
        return completableFuture.get(getTestProperties().getClientTimeout(), TimeUnit.MILLISECONDS);
    }
}
