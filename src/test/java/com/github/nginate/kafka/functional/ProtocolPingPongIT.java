package com.github.nginate.kafka.functional;

import com.github.nginate.kafka.KafkaClusterClient;
import com.github.nginate.kafka.protocol.messages.request.TopicMetadataRequest;
import com.github.nginate.kafka.protocol.messages.response.TopicMetadataResponse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ProtocolPingPongIT extends AbstractFunctionalTest {

    private KafkaClusterClient client;

    @BeforeClass
    public void beforeAbstractFunctionalTest() throws Exception {
        client = new KafkaClusterClient(null);
    }

    @AfterClass
    public void tearDownClient() throws Exception {
        client.close();
    }

    @Test(enabled = false)
    public void testTopicMetadataPingPong() throws Exception {
        TopicMetadataRequest request = TopicMetadataRequest.builder().topic(new String[0]).build();
        TopicMetadataResponse response = client.getTopicMetadata(request, 1000);

        assertThat(response).isNotNull();
    }

    @Override
    protected String getKafkaBrokerVersion() {
        return "0.8";
    }
}
