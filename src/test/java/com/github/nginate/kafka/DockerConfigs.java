package com.github.nginate.kafka;

import com.github.dockerjava.api.model.*;
import com.github.nginate.commons.docker.client.options.CreateContainerOptions;
import lombok.experimental.UtilityClass;

import java.net.SocketException;

@UtilityClass
public final class DockerConfigs {

    private static final int ZOOKEEPER_PORT = 2181;

    public static CreateContainerOptions kafkaContainerConfiguration(String kafkaVersionTag,
            String kafkaHost, Integer kafkaPort, Integer zookeeperPort) throws SocketException {
        return CreateContainerOptions.builder()
                .image("nginate/kafka-docker-bundle:" + kafkaVersionTag)
                .name("kafka-bundle")
                .exposedPort(ExposedPort.tcp(kafkaPort))
                .portBindings(new PortBinding[]{
                        new PortBinding(new Ports.Binding(kafkaPort), ExposedPort.tcp(kafkaPort)),
                        new PortBinding(new Ports.Binding(zookeeperPort), ExposedPort.tcp(ZOOKEEPER_PORT))
                })
                .env("ADVERTISED_PORT", kafkaPort.toString())
                .env("ADVERTISED_HOST", kafkaHost)
                .env("KAFKA_PORT", kafkaPort.toString())
                .env("KAFKA_HEAP_OPTS", "-Xmx256M -Xms128M")
                .env("OVERRIDE_KAFKA_PROPS", "auto.leader.rebalance.enable=true,leader.imbalance.check.interval.seconds=1")
                .logConfig(new LogConfig(LogConfig.LoggingType.DEFAULT))
                .restartPolicy(RestartPolicy.noRestart())
                .build();
    }
}
