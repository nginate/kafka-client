package com.github.nginate.kafka.docker;

import com.github.dockerjava.api.model.*;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.*;

import static java.util.Arrays.stream;

@Data
@Builder(toBuilder = true)
public class ContainerConfig {
    private String name;
    private String hostName;
    private String domainName;
    private String user;
    private long memoryLimit = 0;
    private long memorySwap = 0;
    private int cpuShares = 0;
    private Integer cpuPeriod;
    private String cpuset;
    private boolean attachStdin = false;
    private boolean attachStdout = false;
    private boolean attachStderr = false;
    private String[] portSpecs;
    private boolean tty = false;
    private boolean stdinOpen = false;
    private boolean stdInOnce = false;
    @Singular("env")
    private Map<String, String> env;
    private String[] cmd;
    private String[] entrypoint;
    private String image;
    @Singular("volume")
    private List<Volume> volumes;
    private String workingDir;
    private String macAddress;
    private boolean networkDisabled = false;
    @Singular("exposedPort")
    private List<ExposedPort> exposedPorts;
    private Map<String, String> labels;
    private String cpusetMems;
    private Integer blkioWeight;
    private Boolean oomKillDisable = false;

    private Bind[] binds;
    private Link[] links;
    private LxcConf[] lxcConf;
    private LogConfig logConfig;
    private PortBinding[] portBindings;
    private boolean publishAllPorts;
    private boolean privileged;
    private boolean readonlyRootfs;
    private String[] dns;
    private String[] dnsSearch;
    @Singular("volumeFrom")
    private List<VolumesFrom> volumesFrom;
    private String containerIDFile;
    private Capability[] capAdd;
    private Capability[] capDrop;
    private RestartPolicy restartPolicy;
    private String networkMode;
    private Device[] devices;
    private String[] extraHosts;
    private Ulimit[] ulimits;
    private String pidMode;

    public static class ContainerConfigBuilder {
        public ContainerConfigBuilder oneToOnePortBindings(Integer... ports){
            portBindings = stream(ports)
                    .map(port -> new PortBinding(new Ports.Binding(port), ExposedPort.tcp(port)))
                    .toArray(PortBinding[]::new);
            return this;
        }
    }
}
