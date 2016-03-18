package com.github.nginate.kafka.functional;

import com.github.nginate.kafka.core.KafkaBrokerClient;
import com.github.nginate.kafka.protocol.messages.response.DescribeGroupsResponse;
import com.github.nginate.kafka.protocol.messages.response.MetadataResponse;
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
        client = new KafkaBrokerClient(getKafkaContainer().getIp(), getTestProperties().getKafkaPort());
        waitUntil(10000, 1000, () -> {
            try {
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

    @Test
    public void testDescribeGroupsRequest() throws Exception {
        DescribeGroupsResponse response = await(client.describeGroups());
        assertThat(response).isNotNull();
    }

    @Test
    public void testTopicMetadataRequest() throws Exception {
        MetadataResponse response = await(client.topicMetadata("abc"));
        assertThat(response).isNotNull();
    }

    private <T> T await(CompletableFuture<T> completableFuture)
            throws InterruptedException, ExecutionException, TimeoutException {
        return completableFuture.get(getTestProperties().getClientTimeout(), TimeUnit.MILLISECONDS);
    }
}
