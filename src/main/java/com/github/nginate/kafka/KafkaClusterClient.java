package com.github.nginate.kafka;

import java.io.Closeable;

public interface KafkaClusterClient extends Closeable {

    String getLeader(String topic);

    void getPartitions();

    void getMetadata();
}
