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
    private String name = UUID.randomUUID().toString();
    private String hostName = "";
    private String domainName = "";
    private String user = "";
    private long memoryLimit = 0;
    private long memorySwap = 0;
    private int cpuShares = 0;
    private Integer cpuPeriod = 100000;
    private String cpuset;
    private boolean attachStdin = false;
    private boolean attachStdout = false;
    private boolean attachStderr = false;
    private String[] portSpecs = new String[0];
    private boolean tty = false;
    private boolean stdinOpen = false;
    private boolean stdInOnce = false;
    @Singular("env")
    private Map<String, String> env = new HashMap<>();
    private String[] cmd = new String[0];
    private String[] entrypoint = new String[0];
    private String image;
    private Volumes volumes = new Volumes();
    private String workingDir = "";
    private String macAddress = "";
    private boolean networkDisabled = false;
    @Singular("exposedPort")
    private List<ExposedPort> exposedPorts = new ArrayList<>();
    private Map<String, String> labels = new HashMap<>();
    private String cpusetMems = "";
    private Integer blkioWeight = 0;
    private Boolean oomKillDisable = false;

    private Bind[] binds = new Bind[0];
    private Link[] links = new Link[0];
    private LxcConf[] lxcConf = new LxcConf[0];
    private LogConfig logConfig = new LogConfig(LogConfig.LoggingType.DEFAULT);
    private PortBinding[] portBindings = new PortBinding[0];
    private boolean publishAllPorts;
    private boolean privileged;
    private boolean readonlyRootfs;
    private String[] dns = new String[0];
    private String[] dnsSearch = new String[0];
    private VolumesFrom[] volumesFrom = new VolumesFrom[0];
    private String containerIDFile = "";
    private Capability[] capAdd = new Capability[0];
    private Capability[] capDrop = new Capability[0];
    private RestartPolicy restartPolicy = RestartPolicy.alwaysRestart();
    private String networkMode = "host";
    private Device[] devices = new Device[0];
    private String[] extraHosts = new String[0];
    private Ulimit[] ulimits = new Ulimit[0];
    private String pidMode = "";

    public static class ContainerConfigBuilder {
        public ContainerConfigBuilder oneToOnePortBindings(Integer... ports){
            portBindings = stream(ports)
                    .map(port -> new PortBinding(new Ports.Binding(port), ExposedPort.tcp(port)))
                    .toArray(PortBinding[]::new);
            return this;
        }
    }
}
