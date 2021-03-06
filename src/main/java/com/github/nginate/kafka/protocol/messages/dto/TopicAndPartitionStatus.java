package com.github.nginate.kafka.protocol.messages.dto;

import com.github.nginate.kafka.serialization.Type;
import lombok.Builder;
import lombok.Data;

import static com.github.nginate.kafka.serialization.TypeName.INT16;
import static com.github.nginate.kafka.serialization.TypeName.INT32;
import static com.github.nginate.kafka.serialization.TypeName.STRING;

@Data
@Builder
public class TopicAndPartitionStatus {
    @Type(STRING)
    private String topic;
    @Type(value = INT32, order = 1)
    private Integer partition;
    @Type(value = INT16, order = 2)
    private Short partitionErrorCode;
}
