package com.github.nginate.kafka.zookeeper;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ZkBrokerInfo {
    private Integer brokerId;
    @JsonProperty("jmx_port")
    private Integer jmxPort;
    private Long timestamp;
    private String[] endpoints;
    private String host;
    private Integer version;
    private Integer port;
}
