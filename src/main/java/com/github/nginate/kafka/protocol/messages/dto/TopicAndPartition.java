package com.github.nginate.kafka.protocol.messages.dto;

import com.github.nginate.kafka.protocol.types.Type;
import com.github.nginate.kafka.protocol.types.TypeName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.github.nginate.kafka.protocol.types.TypeName.INT32;
import static com.github.nginate.kafka.protocol.types.TypeName.STRING;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TopicAndPartition {
    @Type(STRING)
    private String topic;
    @Type(value = INT32, order = 1)
    private Integer partition;
}
