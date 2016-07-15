package com.github.nginate.kafka.zookeeper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TopicReplicaAssignment {
    private Integer version;
    private Map<Integer, Set<Integer>> partitions;
}
