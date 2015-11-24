package com.github.nginate.kafka.functional;

import com.github.nginate.kafka.protocol.messages.request.TopicMetadataRequest;
import com.github.nginate.kafka.protocol.messages.response.MetadataResponse;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ProtocolPingPongTests extends AbstractFunctionalTest {

    @Test
    public void testTopicMetadataPingPong() throws Exception {
        TopicMetadataRequest request = TopicMetadataRequest.builder().topic(new String[0]).build();
        MetadataResponse response = getKafkaClusterClient().request(request, 1000);

        assertThat(response).isNotNull();
    }
}
