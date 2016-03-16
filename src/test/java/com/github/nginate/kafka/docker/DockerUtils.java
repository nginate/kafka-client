package com.github.nginate.kafka.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.api.model.VolumesFrom;
import lombok.experimental.UtilityClass;

import static com.github.nginate.kafka.util.CollectionUtils.isBlank;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang.ArrayUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotBlank;

@UtilityClass
public class DockerUtils {

    public static CreateContainerCmd createContainer(DockerClient dockerClient, ContainerConfig config) {
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
}
