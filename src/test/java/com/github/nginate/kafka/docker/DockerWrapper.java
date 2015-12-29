package com.github.nginate.kafka.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.nginate.kafka.docker.exceptions.ContainerStateException;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static com.github.nginate.kafka.util.CollectionUtils.isBlank;
import static com.github.nginate.kafka.util.WaitUtil.waitUntil;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang.ArrayUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotBlank;

@Slf4j
@RequiredArgsConstructor
public class DockerWrapper {
    private final LogContainerResultCallback logResultCallback = new LogContainerResultCallback();
    private final DockerClient dockerClient;
    private final ContainerConfig containerConfiguration;

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
                Container container = existing.get();
                containerId = container.getId();
            } else {
                // if we don't have created container check image exists locally
                if (!localImageExists()) {
                    // and pull if not
                    dockerClient.pullImageCmd(containerConfiguration.getImage()).exec(new PullImageResultCallback())
                            .awaitSuccess();
                }
                CreateContainerCmd containerCmd = createContainer(dockerClient, containerConfiguration);
                containerId = containerCmd.exec().getId();
                dockerClient.startContainerCmd(containerId).exec();
                awaitStarted();
            }
        } else {
            throw new ContainerStateException("Container is already running");
        }
    }

    private CreateContainerCmd createContainer(DockerClient dockerClient, ContainerConfig config) {
        CreateContainerCmd createContainerCmd =
                dockerClient.createContainerCmd(config.getImage())
                        .withAttachStderr(config.isAttachStderr())
                        .withAttachStdin(config.isAttachStdin())
                        .withAttachStdout(config.isAttachStdout())
                        .withCpuShares(config.getCpuShares())
                        .withExposedPorts(config.getExposedPorts().toArray(new ExposedPort[config.getExposedPorts().size()]))
                        .withLogConfig(config.getLogConfig())
                        .withMemoryLimit(config.getMemoryLimit())
                        .withMemorySwap(config.getMemorySwap())
                        .withNetworkDisabled(config.isNetworkDisabled())
                        .withPrivileged(config.isPrivileged())
                        .withPublishAllPorts(config.isPublishAllPorts())
                        .withReadonlyRootfs(config.isReadonlyRootfs())
                        .withRestartPolicy(config.getRestartPolicy())
                        .withStdInOnce(config.isStdInOnce())
                        .withStdinOpen(config.isStdinOpen())
                        .withTty(config.isTty());
        if (!isEmpty(config.getBinds())) {
            createContainerCmd.withBinds(config.getBinds());
        }
        ofNullable(config.getBlkioWeight()).ifPresent(createContainerCmd::withBlkioWeight);
        if (!isEmpty(config.getCapAdd())) {
            createContainerCmd.withCapAdd(config.getCapAdd());
        }
        if (!isEmpty(config.getCapDrop())){
            createContainerCmd.withCapDrop(config.getCapDrop());
        }
        if (!isEmpty(config.getCmd())) {
            createContainerCmd.withCmd(config.getCmd());
        }
        ofNullable(config.getContainerIDFile()).ifPresent(createContainerCmd::withContainerIDFile);
        ofNullable(config.getCpuPeriod()).ifPresent(createContainerCmd::withCpuPeriod);
        ofNullable(config.getCpuset()).ifPresent(createContainerCmd::withCpuset);
        ofNullable(config.getCpusetMems()).ifPresent(createContainerCmd::withCpusetMems);
        if (!isEmpty(config.getDevices())) {
            createContainerCmd.withDevices(config.getDevices());
        }
        if (!isEmpty(config.getDns())) {
            createContainerCmd.withDns(config.getDns());
        }
        if (!isEmpty(config.getDnsSearch())) {
            createContainerCmd.withDnsSearch(config.getDnsSearch());
        }
        if (!isEmpty(config.getEntrypoint())) {
            createContainerCmd.withEntrypoint(config.getEntrypoint());
        }
        if (!isBlank(config.getEnv())) {
            createContainerCmd.withEnv(config.getEnv().entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).toArray(String[]::new));
        }
        if (!isEmpty(config.getExtraHosts())) {
            createContainerCmd.withExtraHosts(config.getExtraHosts());
        }
        if (!isBlank(config.getLabels())) {
            createContainerCmd.withLabels(config.getLabels());
        }
        if (!isEmpty(config.getLinks())) {
            createContainerCmd.withLinks(config.getLinks());
        }
        if (!isEmpty(config.getLxcConf())) {
            createContainerCmd.withLxcConf(config.getLxcConf());
        }
        if (isNotBlank(config.getMacAddress())) {
            createContainerCmd.withMacAddress(config.getMacAddress());
        }
        if (config.getOomKillDisable() != null) {
            createContainerCmd.withOomKillDisable(config.getOomKillDisable());
        }
        if (isNotBlank(config.getPidMode())) {
            createContainerCmd.withPidMode(config.getPidMode());
        }
        if (!isEmpty(config.getPortBindings())) {
            createContainerCmd.withPortBindings(config.getPortBindings());
        }
        if (!isEmpty(config.getPortSpecs())) {
            createContainerCmd.withPortSpecs(config.getPortSpecs());
        }
        if (!isEmpty(config.getUlimits())) {
            createContainerCmd.withUlimits(config.getUlimits());
        }
        if (!isBlank(config.getVolumes())) {
            createContainerCmd.withVolumes(config.getVolumes().toArray(new Volume[config.getVolumes().size()]));
        }
        if (!isBlank(config.getVolumesFrom())) {
            createContainerCmd.withVolumesFrom(config.getVolumesFrom().toArray(new VolumesFrom[config.getVolumesFrom().size()]));
        }
        if (isNotBlank(config.getDomainName())) {
            createContainerCmd.withDomainName(config.getDomainName());
        }
        if (isNotBlank(config.getHostName())) {
            createContainerCmd.withHostName(config.getHostName());
        }
        if (isNotBlank(config.getName())) {
            createContainerCmd.withName(config.getName());
        }
        if (isNotBlank(config.getUser())) {
            createContainerCmd.withUser(config.getUser());
        }
        if (isNotBlank(config.getWorkingDir())) {
            createContainerCmd.withWorkingDir(config.getWorkingDir());
        }
        return createContainerCmd;
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
            dockerClient.removeContainerCmd(containerId).withRemoveVolumes(true).withForce(true).exec();
            awaitRemoved();
        } else {
            tryWithLocalContainer(container ->
                    dockerClient.removeContainerCmd(container.getId()).withRemoveVolumes(true).withForce(true));
        }
    }

    /**
     * Simple write container logs to application logger
     */
    @Synchronized
    public void writeLogs(){
        if (containerId != null) {
            dockerClient.logContainerCmd(containerId).exec(logResultCallback);
        } else {
            tryWithLocalContainer(container -> dockerClient.logContainerCmd(container.getId()).exec(logResultCallback));
        }
    }

    public String getName() {
        return Optional.ofNullable(containerConfiguration.getName()).orElseGet(() -> inspect().getName());
    }

    public String getImageName() {
        return containerConfiguration.getImage();
    }

    public String getContainerIp() {
        return inspect().getNetworkSettings().getIpAddress();
    }

    public InspectContainerResponse.ContainerState getState() {
        return inspect().getState();
    }

    private void awaitStopped() {
        waitUntil(10000, 1000, () -> !inspect().getState().getFinishedAt().isEmpty());
    }

    private void awaitRemoved() {
        waitUntil(10000, 1000, () -> !findLocalContainer().isPresent());
    }

    private void awaitStarted() {
        waitUntil(10000, 1000, () -> inspect().getState().isRunning());
    }

    private void tryWithLocalContainer(Consumer<Container> consumer) {
        Optional<Container> local = findLocalContainer();
        if (local.isPresent()) {
            consumer.accept(local.get());
        } else {
            throw new ContainerStateException("Could not find local container");
        }
    }

    private Optional<Container> findLocalContainer() {
        List<Container> containers = dockerClient.listContainersCmd().withShowAll(true)
                .withFilters(new Filters().withImages(containerConfiguration.getImage()).withContainers(containerConfiguration.getName())).exec();
        if (containers.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(containers.get(0));
    }

    private InspectContainerResponse inspect() {
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
