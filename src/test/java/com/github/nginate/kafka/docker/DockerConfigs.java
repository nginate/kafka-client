package com.github.nginate.kafka.docker;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.LogConfig;
import com.github.dockerjava.api.model.RestartPolicy;
import com.google.common.base.Throwables;
import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

@UtilityClass
public final class DockerConfigs {

    private static final int ZOOKEEPER_PORT = 2181;
    private static final Integer KAFKA_PORT = 9092;

    public static ContainerConfig kafkaContainerConfiguration() {
        return ContainerConfig.builder()
                .image("spotify/kafka")
                .name("kafka-bundle")
                .exposedPort(ExposedPort.tcp(ZOOKEEPER_PORT))
                .exposedPort(ExposedPort.tcp(KAFKA_PORT))
                .oneToOnePortBindings(ZOOKEEPER_PORT, KAFKA_PORT)
                .env("ADVERTISED_PORT", KAFKA_PORT.toString())
                .env("ADVERTISED_HOST", getHostIp())
                .env("KAFKA_HEAP_OPTS", "\"-Xmx256M -Xms128M\"")
                .logConfig(new LogConfig(LogConfig.LoggingType.DEFAULT))
                .restartPolicy(RestartPolicy.alwaysRestart())
                .build();
    }

    private static String getHostIp(){
        try {
            URL whatIsMyIp = new URL("http://checkip.amazonaws.com");
            try (BufferedReader in = new BufferedReader(new InputStreamReader(whatIsMyIp.openStream()))){
                return in.readLine();
            }
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }
}
