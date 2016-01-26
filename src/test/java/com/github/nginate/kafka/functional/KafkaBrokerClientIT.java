package com.github.nginate.kafka.functional;

import com.github.nginate.kafka.core.KafkaBrokerClient;
import com.github.nginate.kafka.protocol.messages.response.MetadataResponse;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;


public class KafkaBrokerClientIT extends AbstractFunctionalTest {

    private KafkaBrokerClient client;

    @BeforeClass
    public void prepareClient() throws Exception {
        client = new KafkaBrokerClient(getTestProperties().getKafkaHost(), 9092);
        client.connect();
    }

    /*@Test
    public void testDescribeGroupsRequest() throws Exception {
        DescribeGroupsResponse response = await(client.describeGroups());
        assertThat(response).isNotNull();
    }*/

    @Test
    public void testTopicMetadataRequest() throws Exception {
        MetadataResponse response = await(client.topicMetadata("abc"));
        assertThat(response).isNotNull();
    }

    private <T> T await(CompletableFuture<T> completableFuture)
            throws InterruptedException, ExecutionException, TimeoutException {
        return completableFuture.get(5000, TimeUnit.MILLISECONDS);
    }
}
