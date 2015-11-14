package com.github.nginate.kafka.protocol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Partition {
    private String topic;
    private int partition;
    private KafkaNode leader;
    private KafkaNode[] replicas;
    private KafkaNode[] inSyncReplicas;
}
