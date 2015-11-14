package com.github.nginate.kafka.core;

import java.io.Closeable;
import java.net.InetAddress;

public interface KafkaCLusterConnectionPool extends Closeable{
    KafkaBrokerConnection connect(InetAddress address);

    void releaseConnection(KafkaBrokerConnection connection);

    void closeConnection(KafkaBrokerConnection connection);

    void setMaxConnections(int maxConnections);

    int getMaxConnections();

    void setConnectionEventListener(ConnectionEventListener eventListener);

    /**
     * Closes all unused pooled connections.
     * Exceptions while closing are written to the log stream (if set).
     */
    void dispose();
}