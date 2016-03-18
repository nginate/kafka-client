package com.github.nginate.kafka.docker;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.nginate.kafka.docker.exceptions.ContainerStateException;
import com.github.nginate.kafka.docker.exceptions.DockerException;
import com.github.nginate.kafka.docker.exceptions.ImageNotFoundException;

import java.util.function.Function;

public interface DockerContainer {

    /**
     * Create new container from existing configuration
     * @throws DockerException if config is not valid
     */
    void create() throws DockerException;

    /**
     * Starts precreated container
     * @throws ContainerStateException if container not exists or is already running
     */
    void start() throws ContainerStateException;

    /**
     * Stops running container
     * @throws ContainerStateException if container not present or is not running
     */
    void stop() throws ContainerStateException;

    /**
     * Update/load latest image from registry
     * @throws ImageNotFoundException if image cannot be found
     */
    void pullImage() throws ImageNotFoundException;

    /**
     * Remove container with its volumes
     * @throws ContainerStateException if container is not present or is running
     */
    void remove() throws ContainerStateException;

    /**
     * Kill running container
     * @throws ContainerStateException if container not present or is not running
     */
    void forceStop() throws ContainerStateException;

    /**
     * @return true if container exists and is running
     */
    boolean isRunning();

    /**
     * @return true if image, provided via container config is present locally
     */
    boolean localImageExists();

    void printLogs();

    /**
     * Inspect container and get a piece of available info
     * @param resultExtractor function to get some data from inspect response
     * @param <T> type of result
     * @return extracted result
     */
    <T> T fromContainerInfo(Function<InspectContainerResponse, T> resultExtractor);

    /**
     * Most common docker container info, requested from docker info - ip address for dicrect communication
     * @return container ip
     */
    String getIp();
}
