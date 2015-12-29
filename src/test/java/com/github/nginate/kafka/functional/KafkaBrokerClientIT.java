package com.github.nginate.kafka.functional;

import com.github.nginate.kafka.core.KafkaBrokerClient;
import com.github.nginate.kafka.protocol.ApiKeys;
import com.github.nginate.kafka.protocol.messages.request.TopicMetadataRequest;
import com.github.nginate.kafka.protocol.messages.response.MetadataResponse;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class KafkaBrokerClientIT extends AbstractFunctionalTest {

    private KafkaBrokerClient client;

    @BeforeClass
    public void prepareClient() throws Exception {
        client = new KafkaBrokerClient(getTestProperties().getKafkaHost(), 9092);
        client.connect();
    }

    @Test
    public void testTopicMetadataPingPong() throws Exception {
        TopicMetadataRequest request = TopicMetadataRequest.builder()
                .topic(new String[]{"abc"})
                .build();
        request.setCorrelationId(2);
        request.setApiVersion(ApiKeys.METADATA.getId());
        request.setClientId("4");
        MetadataResponse response = client.sendAndReceive(request, MetadataResponse.class).join();
        assertThat(response).isNotNull();
    }
}
