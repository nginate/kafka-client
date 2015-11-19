package com.github.nginate.kafka.docker;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.core.command.CreateContainerCmdImpl;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class DockerConfigs {

    public static CreateContainerCmd kafkaContainerConfiguration() {
        return new CreateContainerCmdImpl(null, "kafka:latest");
    }

    public static CreateContainerCmd zookeeperContainerConfiguration() {
        return new CreateContainerCmdImpl(null, "zookeeper:latest");
    }
}
