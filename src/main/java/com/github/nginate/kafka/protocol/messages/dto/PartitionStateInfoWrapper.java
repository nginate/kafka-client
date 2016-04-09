package com.github.nginate.kafka.protocol.messages.dto;

import static com.github.nginate.kafka.protocol.types.TypeName.WRAPPER;

import lombok.Builder;
import lombok.Data;

import com.github.nginate.kafka.protocol.types.Type;

@Data
@Builder
public class PartitionStateInfoWrapper {
    @Type(WRAPPER)
    private TopicAndPartition topicAndPartition;
    @Type(value = WRAPPER, order = 1)
    private PartitionStateInfo partitionStateInfo;
}
