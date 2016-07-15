package com.github.nginate.kafka.zookeeper.dto;

import lombok.*;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TopicConfiguration {
    private Integer version;
    private Map<String, String> config;
}
