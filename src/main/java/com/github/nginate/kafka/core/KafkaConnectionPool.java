package com.github.nginate.kafka.core;

import com.github.nginate.kafka.exceptions.ConnectionException;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.net.InetAddress;

@Slf4j
public class KafkaConnectionPool implements Closeable, ConnectionEventListener {

    @Getter
    @Setter
    private volatile int maxConnections;

    @Synchronized
    public KafkaConnection connect(InetAddress address) throws ConnectionException {
        return KafkaConnection.builder().address(address).listener(this).build();
    }

    @Synchronized
    public void releaseConnection(KafkaConnection connection) throws ConnectionException {
        connection.close();
    }

    @Synchronized
    public void closeConnection(KafkaConnection connection) throws ConnectionException {
        connection.close();
    }

    /**
     * Closes all unused pooled connections.
     * Exceptions while closing are written to the log stream (if set).
     */
    @Synchronized
    public void dispose() {

    }

    @Override
    public void close() {
        dispose();
    }

    @Synchronized
    @Override
    public void onConnectionClosed(KafkaConnection connection) {
        log.warn("Connection to {} was closed externally", connection.getAddress());
        releaseConnection(connection);
    }

    @Synchronized
    @Override
    public void onConnectionError(KafkaConnection connection, Throwable error) {
        log.error("Error occurred communicating with {}", connection.getAddress(), error);
        releaseConnection(connection);
    }

    @Synchronized
    private void checkConnections() {

    }
}
