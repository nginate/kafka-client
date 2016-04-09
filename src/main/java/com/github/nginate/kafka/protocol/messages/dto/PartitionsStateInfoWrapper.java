package com.github.nginate.kafka.protocol.messages.dto;

import com.github.nginate.kafka.protocol.types.Type;
import lombok.Builder;
import lombok.Data;

import static com.github.nginate.kafka.protocol.types.TypeName.WRAPPER;

@Data
@Builder
public class PartitionsStateInfoWrapper {
    @Type(WRAPPER)
    private TopicAndPartition topicAndPartition;
    @Type(value = WRAPPER, order = 1)
    private PartitionStateInfo[] partitionStateInfos;
}
