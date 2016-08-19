package com.github.nginate.kafka.functional;

import com.github.nginate.kafka.core.ClusterConfiguration;
import com.github.nginate.kafka.core.KafkaClusterClient;
import com.github.nginate.kafka.core.KafkaClusterClientImpl;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.charset.Charset;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.github.nginate.commons.lang.await.Await.waitUntil;
import static com.github.nginate.kafka.util.StringUtils.format;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class KafkaClusterClientIT extends AbstractFunctionalTest {
    private KafkaClusterClient kafkaClusterClient;

    @BeforeClass(dependsOnMethods = "initDockerContainer")
    public void prepareClient() throws Exception {
        ClusterConfiguration clusterConfiguration = ClusterConfiguration.builder()
                .zookeeperUrl(format("{}:{}", getZookeeperHost(), getZookeeperPort()))
                .pollingInterval(2000L)
                .produceWaitOnMetadataTimeout(10000)
                .defaultReplicationFactor(1)
                .defaultPartitions(1)
                .consumerGroupId(UUID.randomUUID().toString())
                .build();
        kafkaClusterClient = new KafkaClusterClientImpl(clusterConfiguration, payload ->
                payload.toString().getBytes(Charset.forName("UTF-8")));
        waitUntil(10000, 1000, () -> getZkClient().countChildren("/brokers/ids") > 0);
        waitUntil(10000, 1000, () -> kafkaClusterClient.isClusterOperational());
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClient() throws Exception {
        kafkaClusterClient.close();
    }

    @Test
    public void testProduceMessage() throws Exception {
        String stringMessage = "produce message";
        String topic = "test_topic";

        CompletableFuture<Void> sendFuture = kafkaClusterClient.send(topic, stringMessage);
        await(sendFuture, 180000);

        CompletableFuture<String> listenerFuture = new CompletableFuture<>();
        kafkaClusterClient.subscribeWith(topic,
                rawData -> new String(rawData, Charset.forName("UTF-8")),
                (value) -> listenerFuture.complete(value));

        String retrieved = listenerFuture.get(20, TimeUnit.SECONDS);
        assertThat(retrieved).isEqualTo(stringMessage);
    }

    @Override
    protected String getKafkaBrokerVersion() {
        return "0.10";
    }
}
