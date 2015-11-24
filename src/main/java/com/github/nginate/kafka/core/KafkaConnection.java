package com.github.nginate.kafka.core;

import com.github.nginate.kafka.exceptions.CommunicationException;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.io.Closeable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

@Builder(toBuilder = true)
public class KafkaConnection implements Closeable {
    @Getter
    private InetAddress address;
    @Singular("listener")
    private List<ConnectionEventListener> listeners = new ArrayList<>();

    public byte[] sendAndReceive(byte[] request) throws CommunicationException {
        return new byte[0];
    }

    @Override
    public void close() {

    }
}
