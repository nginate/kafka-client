package com.github.nginate.kafka;

import java.io.Closeable;
import java.net.InetAddress;

public interface KafkaCLusterConnectionPool extends Closeable{
    KafkaBrokerConnection connect(InetAddress address);

    void release(KafkaBrokerConnection connection);
}
