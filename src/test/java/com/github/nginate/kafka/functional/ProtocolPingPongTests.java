package com.github.nginate.kafka.functional;

import com.github.nginate.kafka.protocol.messages.request.TopicMetadataRequest;
import org.testng.annotations.Test;

public class ProtocolPingPongTests extends AbstractFunctionalTest {

    @Test
    public void testTopicMetadataPingPong() throws Exception {
        TopicMetadataRequest request = TopicMetadataRequest.builder().topic(new String[]{"topic"}).build();
    }
}
