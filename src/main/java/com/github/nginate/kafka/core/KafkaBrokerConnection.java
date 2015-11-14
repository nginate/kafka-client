package com.github.nginate.kafka.core;

import java.io.Closeable;
import java.net.InetAddress;

public interface KafkaBrokerConnection extends Closeable {
    InetAddress getBrokerAddress();

    void addConnectionEventListener(ConnectionEventListener eventListener);

    void removeConnectionEventListener(ConnectionEventListener eventListener);
}
