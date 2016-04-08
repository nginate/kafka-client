package com.github.nginate.kafka;

import lombok.Data;

@Data
public class TestProperties {
    private String dockerUrl;
    private String kafkaHost;
    private Integer kafkaPort;
    private Integer zookeeperPort;
    private Integer clientTimeout;
}
