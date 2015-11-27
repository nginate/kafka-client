package com.github.nginate.kafka.core;

import com.github.nginate.kafka.exceptions.CommunicationException;
import com.github.nginate.kafka.network.client.BinaryTcpClient;
import com.github.nginate.kafka.network.client.BinaryTcpClientConfig;
import com.github.nginate.kafka.protocol.messages.Request;
import com.github.nginate.kafka.protocol.messages.Response;
import lombok.Builder;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;

@Builder(toBuilder = true)
public class KafkaBrokerClient implements Closeable {
    private final BinaryTcpClient binaryTcpClient;

    public KafkaBrokerClient(String host, int port) {
        BinaryTcpClientConfig config = BinaryTcpClientConfig.custom().host(host).port(port).build();
        binaryTcpClient = new BinaryTcpClient(config);
    }

    public void connect() {
        binaryTcpClient.connect();
    }

    public void send(Request request) {
        Response response = (Response) binaryTcpClient.request(request).join();
        // TODO: check ack from broker
    }

    public <T extends Response> CompletableFuture<T> sendAndReceive(Request request, Class<T> responseClass)
            throws CommunicationException {
        return binaryTcpClient.request(request).thenApply(responseClass::cast);
    }

    @Override
    public void close() {
        binaryTcpClient.close();
    }
}
