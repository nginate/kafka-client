package com.github.nginate.kafka.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.nginate.kafka.docker.exceptions.ContainerStateException;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;

import java.util.Optional;

import static com.github.nginate.kafka.util.BeanUtil.copyDockerDto;
import static com.github.nginate.kafka.util.WaitUtil.waitUntil;

@Slf4j
@RequiredArgsConstructor
public class DockerWrapper {
    private final DockerClient dockerClient;
    private final CreateContainerCmd containerConfiguration;

    private volatile String containerId;
    private volatile boolean started;

    /**
     * Creates and starts container from given configuration. Blocking wait for container start
     */
    @Synchronized
    public void start(){
        if (!started) {
            // If we already have local container just start it
            Optional<Container> existing = findLocalContainer();
            if (existing.isPresent()) {
                containerId = existing.get().getId();
                dockerClient.startContainerCmd(containerId);
            } else {
                // if we don't have created container check image exists locally
                if (!localImageExists()) {
                    // and pull if not
                    dockerClient.pullImageCmd(containerConfiguration.getImage()).exec(new PullImageResultCallback())
                            .awaitSuccess();
                }
                CreateContainerCmd containerCmd = dockerClient.createContainerCmd(containerConfiguration.getImage());
                copyDockerDto(containerConfiguration, containerCmd);
                containerId = containerCmd.exec().getId();
                awaitStarted();
            }
        } else {
            throw new ContainerStateException("Container is already running");
        }
    }

    /**
     * Ordinal stop execution sending stop signal via docker daemon
     */
    @Synchronized
    public void stop(){
        if (started) {
            dockerClient.stopContainerCmd(containerId).exec();
            awaitStopped();
        } else {
            throw new ContainerStateException("Container is not running");
        }
    }

    /**
     * Immediately stop container
     */
    @Synchronized
    public void kill(){
        if (started) {
            dockerClient.killContainerCmd(containerId).exec();
            awaitStopped();
        } else {
            throw new ContainerStateException("Container is not running");
        }
    }

    /**
     * Ordinal removal. Can be executed only on stopped containers
     */
    @Synchronized
    public void remove(){
        if (!started) {
            dockerClient.removeContainerCmd(containerId).exec();
            awaitRemoved();
        }else{
            throw new ContainerStateException("Container is running");
        }
    }

    /**
     * Remove container even while running
     */
    @Synchronized
    public void purge(){
        if (containerId != null) {
            dockerClient.removeContainerCmd(containerId).withForce(true).exec();
            awaitRemoved();
        } else {
            Optional<Container> local = findLocalContainer();
            if (local.isPresent()) {
                dockerClient.removeContainerCmd(local.get().getId()).withForce(true);
            } else {
                throw new ContainerStateException("Could not find container to purge");
            }
        }
    }

    /**
     * Simple write container logs to application logger
     */
    @Synchronized
    public void writeLogs(){
        dockerClient.logContainerCmd(containerId).exec(new LogContainerResultCallback());
    }

    private void awaitStopped() {
        waitUntil(10000, 1000, () -> !inspect(containerId).getState().getFinishedAt().isEmpty());
    }

    private void awaitRemoved() {
        waitUntil(10000, 1000, () -> !findLocalContainer().isPresent());
    }

    private void awaitStarted() {
        waitUntil(10000, 1000, () -> inspect(containerId).getState().isRunning());
    }

    private Optional<Container> findLocalContainer() {
        return dockerClient.listContainersCmd().withShowAll(true).exec().stream()
                .filter(container -> container.getImage().equals(containerConfiguration.getImage()) &&
                        ArrayUtils.contains(container.getNames(), containerConfiguration.getName())).findAny();
    }

    private InspectContainerResponse inspect(String containerId) {
        return dockerClient.inspectContainerCmd(containerId).exec();
    }

    private boolean localImageExists() {
        return dockerClient.listImagesCmd().exec().stream()
                .filter(image -> ArrayUtils.contains(image.getRepoTags(), containerConfiguration.getImage()))
                .findAny().isPresent();
    }

    private InspectContainerResponse getContainerDetails() {
        return dockerClient.inspectContainerCmd(containerId).exec();
    }
}
