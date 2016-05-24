package com.github.nginate.kafka.core;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ClusterConfiguration {
    private String zookeeperUrl;
    private Short requiredAcks;
    private Integer produceTimeout;
    private Long pollingInterval;

    public static ClusterConfiguration defaultConfig() {
        return ClusterConfiguration.builder().build();
    }
}
