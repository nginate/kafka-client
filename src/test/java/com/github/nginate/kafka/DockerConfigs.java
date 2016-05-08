package com.github.nginate.kafka;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.LogConfig;
import com.github.dockerjava.api.model.RestartPolicy;
import com.github.nginate.commons.docker.client.options.CreateContainerOptions;
import lombok.experimental.UtilityClass;

import java.net.SocketException;

@UtilityClass
public final class DockerConfigs {

    public static CreateContainerOptions kafkaContainerConfiguration(String kafkaHost, Integer kafkaPort,
            Integer zookeeperPort) throws SocketException {
        return CreateContainerOptions.builder()
                .image("nginate/kafka-docker-bundle:0.8")
                .name("kafka-bundle")
                .exposedPort(ExposedPort.tcp(zookeeperPort))
                .exposedPort(ExposedPort.tcp(kafkaPort))
                .oneToOnePortBindings(zookeeperPort, kafkaPort)
                .env("ADVERTISED_PORT", kafkaPort.toString())
                .env("ADVERTISED_HOST", kafkaHost)
                .env("KAFKA_HEAP_OPTS", "-Xmx256M -Xms128M")
                .logConfig(new LogConfig(LogConfig.LoggingType.DEFAULT))
                .restartPolicy(RestartPolicy.alwaysRestart())
                .networkMode("host")
                .build();
    }
}
