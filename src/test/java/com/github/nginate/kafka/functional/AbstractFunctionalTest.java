package com.github.nginate.kafka.functional;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.nginate.kafka.KafkaClusterClient;
import com.github.nginate.kafka.docker.DockerWrapper;
import lombok.Getter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import static com.github.nginate.kafka.docker.DockerConfigs.kafkaContainerConfiguration;

public abstract class AbstractFunctionalTest {

    private DockerWrapper kafkaContainer;
    @Getter
    private KafkaClusterClient kafkaClusterClient;

    @BeforeClass
    public void beforeAbstractFunctionalTest() throws Exception {
        DockerClient dockerClient = DockerClientBuilder.getInstance("http://127.0.0.1:2375").build();
        kafkaContainer = new DockerWrapper(dockerClient, kafkaContainerConfiguration());
        kafkaContainer.start();
    }

    @AfterClass
    public void afterAbstractFunctionalTest() throws Exception {
        kafkaContainer.purge();
    }
}
