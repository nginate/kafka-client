package com.github.nginate.kafka.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Filters;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.nginate.kafka.docker.exceptions.ContainerStateException;
import com.github.nginate.kafka.docker.exceptions.DockerException;
import com.github.nginate.kafka.docker.exceptions.ImageNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.nginate.kafka.util.StringUtils.format;
import static com.github.nginate.kafka.util.WaitUtil.waitUntil;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.stream;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DockerWrapper implements DockerContainer {
    private final DockerClient dockerClient;
    private final ContainerConfig containerConfiguration;
    private final Consumer<String> logConsumer;

    private volatile String containerId;

    public Builder toBuilder() {
        return new Builder()
                .dockerClient(dockerClient)
                .containerConfiguration(containerConfiguration)
                .logConsumer(logConsumer);
    }

    @Override
    public void create() throws DockerException {
        removeContainerWithNameIfExists();
        CreateContainerResponse response = DockerUtils.createContainer(dockerClient, containerConfiguration).exec();
        stream(Optional.ofNullable(response.getWarnings()).orElse(new String[0])).forEach(logConsumer);
        containerId = response.getId();
    }

    @Override
    public void start() throws ContainerStateException {
        checkContainerExists();
        dockerClient.startContainerCmd(containerId).exec();
        awaitStarted();
    }

    @Override
    public void stop() throws ContainerStateException {
        checkContainerExists();
        dockerClient.stopContainerCmd(containerId).exec();
        awaitStopped();
    }

    @Override
    public void pullImage() throws ImageNotFoundException {
        PullImageResultCallback callback = dockerClient.pullImageCmd(containerConfiguration.getImage())
                .exec(new PullImageResultCallback());
        callback.awaitSuccess();
    }

    @Override
    public void remove() throws ContainerStateException {
        checkContainerExists();
        dockerClient.removeContainerCmd(containerId).withRemoveVolumes(true).withForce(true).exec();
    }

    @Override
    public void forceStop() throws ContainerStateException {
        checkContainerExists();
        dockerClient.killContainerCmd(containerId).exec();
        awaitStopped();
    }

    @Override
    public boolean isRunning() {
        return fromContainerInfo(InspectContainerResponse::getState).isRunning();
    }

    @Override
    public boolean localImageExists() {
        return false;
    }

    @Override
    public void printLogs() {
        dockerClient.logContainerCmd(containerId)
                .withStdOut()
                .withStdErr()
                .withFollowStream()
                .exec(new DockerLogger(logConsumer))
                .awaitCompletion();
    }

    @Override
    public <T> T fromContainerInfo(Function<InspectContainerResponse, T> resultExtractor) {
        checkContainerExists();
        return resultExtractor.apply(dockerClient.inspectContainerCmd(containerId).exec());
    }

    private void checkState(Predicate<InspectContainerResponse.ContainerState> statePredicate) {
        checkContainerExists();
        InspectContainerResponse.ContainerState containerState = fromContainerInfo(InspectContainerResponse::getState);
        if (!statePredicate.test(containerState)) {
            throw new ContainerStateException(format("Container has wrong state : {}", containerState));
        }
    }

    private void checkContainerExists() {
        Optional.ofNullable(containerId).orElseThrow(() -> new ContainerStateException("Container is not present"));
    }

    private void removeContainerWithNameIfExists() {
        String name = containerConfiguration.getName();
        List<Container> containerList = dockerClient.listContainersCmd()
                .withShowAll(true)
                .withFilters(new Filters().withFilter("name", name))
                .exec();
        if (!containerList.isEmpty()) {
            log.warn("Container with name {} already exists. Removing it", name);
            dockerClient.removeContainerCmd(name).withForce(true).withRemoveVolumes(true).exec();
        }
    }

    private void awaitStarted() {
        waitUntil(10000, 1000, () -> fromContainerInfo(InspectContainerResponse::getState).isRunning());
    }

    private void awaitStopped() {
        waitUntil(10000, 1000, () -> !fromContainerInfo(InspectContainerResponse::getState).isRunning());
    }

    public static class Builder {
        private DockerClient dockerClient;
        private ContainerConfig containerConfiguration;
        private Consumer<String> logConsumer;

        public Builder dockerClient(DockerClient dockerClient) {
            this.dockerClient = dockerClient;
            return this;
        }

        public Builder containerConfiguration(ContainerConfig containerConfiguration) {
            this.containerConfiguration = containerConfiguration;
            return this;
        }

        public Builder useSlf4jInfoLogging() {
            logConsumer = log::info;
            return this;
        }

        public Builder useLogFile() {
            //TODO
            return this;
        }

        public Builder logConsumer(Consumer<String> logConsumer) {
            this.logConsumer = logConsumer;
            return this;
        }

        public DockerWrapper build() {
            checkNotNull(dockerClient, "Docker client not provided");
            checkNotNull(containerConfiguration, "Container configuration not provided");

            Consumer<String> logConsumer = Optional.ofNullable(this.logConsumer).orElse(log::debug);
            return new DockerWrapper(dockerClient, containerConfiguration, logConsumer);
        }
    }
}
