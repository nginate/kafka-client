package com.github.nginate.kafka.core;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class ClusterConfiguration {
    private String zookeeperUrl;
    private Short requiredAcks;
    private Integer produceTimeout;
    private Long pollingInterval;
    /**
     * A unique string that identifies the consumer group this consumer belongs to. This property is required if the
     * consumer uses either the group management functionality by using <code>subscribe(topic)</code> or the Kafka-based
     * offset management strategy.
     */
    private String consumerGroupId;
    /**
     * Protocol veriosn specific
     */
    private Integer defaultGeneration;

    public static ClusterConfiguration defaultConfig() {
        return ClusterConfiguration.builder()
                .consumerGroupId(UUID.randomUUID().toString())
                .defaultGeneration(-1)
                .build();
    }
}
