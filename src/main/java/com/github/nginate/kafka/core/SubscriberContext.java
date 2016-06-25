package com.github.nginate.kafka.core;

import com.github.nginate.kafka.protocol.messages.dto.TopicAndPartition;
import com.github.nginate.kafka.protocol.messages.dto.TopicPartitionState;

import java.util.HashMap;
import java.util.Map;

public class SubscriberContext {

    /* the list of partitions currently assigned */
    private final Map<TopicAndPartition, TopicPartitionState> assignment;

    public SubscriberContext() {
        assignment = new HashMap<>();
    }

    public boolean hasUnfetchedPositions() {
        return assignment.values()
                .stream()
                .filter(state -> state.getPosition() == null)
                .findAny()
                .isPresent();
    }

    public void assignPartition(TopicAndPartition topicAndPartition) {
        assignment.put(topicAndPartition, new TopicPartitionState());
    }
}
