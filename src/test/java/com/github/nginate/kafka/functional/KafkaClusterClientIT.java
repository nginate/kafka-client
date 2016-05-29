package com.github.nginate.kafka.functional;

import com.github.nginate.kafka.core.ClusterConfiguration;
import com.github.nginate.kafka.core.KafkaClusterClient;
import com.github.nginate.kafka.core.KafkaClusterClientImpl;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.github.nginate.kafka.util.StringUtils.format;
import static org.assertj.core.api.Assertions.assertThat;

public class KafkaClusterClientIT extends AbstractFunctionalTest {
    private KafkaClusterClient kafkaClusterClient;

    @BeforeClass(dependsOnMethods = "initDockerContainer")
    public void prepareClient() throws Exception {
        ClusterConfiguration clusterConfiguration = ClusterConfiguration.builder()
                .zookeeperUrl(format("{}:{}", getZookeeperHost(), getZookeeperPort()))
                .build();
        kafkaClusterClient = new KafkaClusterClientImpl(clusterConfiguration, payload ->
                payload.toString().getBytes(Charset.forName("UTF-8")));
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClient() throws Exception {
        kafkaClusterClient.close();
    }

    @Test
    public void testProduceMessage() throws Exception {
        String stringMessage = "produce message";
        String topic = "test topic";
        kafkaClusterClient.send(topic, stringMessage);

        CompletableFuture<String> listenerFuture = new CompletableFuture<>();
        kafkaClusterClient.subscribeWith(topic,
                rawData -> new String(rawData, Charset.forName("UTF-8")),
                listenerFuture::complete);

        String retrieved = listenerFuture.get(20, TimeUnit.SECONDS);
        assertThat(retrieved).isEqualTo(stringMessage);
    }

    @Override
    protected String getKafkaBrokerVersion() {
        return "0.10";
    }
}
