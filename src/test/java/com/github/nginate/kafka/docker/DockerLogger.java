package com.github.nginate.kafka.docker;

import com.github.dockerjava.api.DockerClientException;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.async.ResultCallbackTemplate;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

import static com.google.common.base.Charsets.UTF_8;

@RequiredArgsConstructor
public class DockerLogger extends ResultCallbackTemplate<DockerLogger, Frame> {
    private final Consumer<String> logEntryConsumer;

    @Override
    public void onNext(Frame object) {
        logEntryConsumer.accept(new String(object.getPayload(), UTF_8));
    }

    @Override
    public DockerLogger awaitCompletion() throws DockerClientException {
        try {
            return super.awaitCompletion();
        } catch (InterruptedException e) {
            throw new DockerClientException("Could not retrieve all logs from container", e);
        }
    }
}
