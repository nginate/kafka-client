package com.github.nginate.kafka.core;

import java.util.EventListener;

public interface ConnectionEventListener extends EventListener {

    void onConnectionClosed(KafkaBrokerConnection connection);

    void onConnectionError(KafkaBrokerConnection connection, Throwable error);
}
