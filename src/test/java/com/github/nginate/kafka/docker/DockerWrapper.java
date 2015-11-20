package com.github.nginate.kafka.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.nginate.kafka.docker.exceptions.ContainerStateException;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;

import java.util.Optional;
import java.util.function.Consumer;

import static com.github.nginate.kafka.util.WaitUtil.waitUntil;

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
                containerId = existing.get().getId();
                dockerClient.startContainerCmd(containerId);
            } else {
                // if we don't have created container check image exists locally
                if (!localImageExists()) {
                    // and pull if not
                    dockerClient.pullImageCmd(containerConfiguration.getImage()).exec(new PullImageResultCallback())
                            .awaitSuccess();
                }
                CreateContainerCmd containerCmd = createContainer(dockerClient, containerConfiguration);
                containerId = containerCmd.exec().getId();
                awaitStarted();
            }
        } else {
            throw new ContainerStateException("Container is already running");
        }
    }

    private CreateContainerCmd createContainer(DockerClient dockerClient, ContainerConfig config) {
        return dockerClient.createContainerCmd(config.getImage())
                .withAttachStderr(config.isAttachStderr())
                .withAttachStdin(config.isAttachStdin())
                .withAttachStdout(config.isAttachStdout())
                .withBinds(config.getBinds())
                .withBlkioWeight(config.getBlkioWeight())
                .withCapAdd(config.getCapAdd())
                .withCapDrop(config.getCapDrop())
                .withCmd(config.getCmd())
                .withContainerIDFile(config.getContainerIDFile())
                .withCpuPeriod(config.getCpuPeriod())
                .withCpuset(config.getCpuset())
                .withCpusetMems(config.getCpusetMems())
                .withCpuShares(config.getCpuShares())
                .withDevices(config.getDevices())
                .withDns(config.getDns())
                .withDnsSearch(config.getDnsSearch())
                .withDomainName(config.getDomainName())
                .withEntrypoint(config.getEntrypoint())
                .withEnv(config.getEnv().entrySet().stream().map(entry -> entry.getKey()+"="+entry.getValue()).toArray(String[]::new))
                .withExposedPorts(config.getExposedPorts().toArray(new ExposedPort[config.getExposedPorts().size()]))
                .withExtraHosts(config.getExtraHosts())
                .withHostName(config.getHostName())
                .withImage(config.getImage())
                .withLabels(config.getLabels())
                .withLinks(config.getLinks())
                .withLogConfig(config.getLogConfig())
                .withLxcConf(config.getLxcConf())
                .withMacAddress(config.getMacAddress())
                .withMemoryLimit(config.getMemoryLimit())
                .withMemorySwap(config.getMemorySwap())
                .withName(config.getName())
                .withNetworkDisabled(config.isNetworkDisabled())
                .withOomKillDisable(config.getOomKillDisable())
                .withPidMode(config.getPidMode())
                .withPortBindings(config.getPortBindings())
                .withPortSpecs(config.getPortSpecs())
                .withPrivileged(config.isPrivileged())
                .withPublishAllPorts(config.isPublishAllPorts())
                .withReadonlyRootfs(config.isReadonlyRootfs())
                .withRestartPolicy(config.getRestartPolicy())
                .withStdInOnce(config.isStdInOnce())
                .withStdinOpen(config.isStdinOpen())
                .withTty(config.isTty())
                .withUlimits(config.getUlimits())
                .withUser(config.getUser())
                .withVolumes(config.getVolumes().getVolumes())
                .withVolumesFrom(config.getVolumesFrom())
                .withWorkingDir(config.getWorkingDir());
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
            tryWithLocalContainer(container -> dockerClient.removeContainerCmd(container.getId()).withForce(true));
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

    private void awaitStopped() {
        waitUntil(10000, 1000, () -> !inspect(containerId).getState().getFinishedAt().isEmpty());
    }

    private void awaitRemoved() {
        waitUntil(10000, 1000, () -> !findLocalContainer().isPresent());
    }

    private void awaitStarted() {
        waitUntil(10000, 1000, () -> inspect(containerId).getState().isRunning());
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
