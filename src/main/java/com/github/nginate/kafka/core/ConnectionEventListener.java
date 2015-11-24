package com.github.nginate.kafka.core;

import java.util.EventListener;

public interface ConnectionEventListener extends EventListener {

    void onConnectionClosed(KafkaConnection connection);

    void onConnectionError(KafkaConnection connection, Throwable error);
}
