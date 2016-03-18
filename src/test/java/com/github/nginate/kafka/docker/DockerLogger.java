package com.github.nginate.kafka.docker;

import com.github.dockerjava.api.DockerClientException;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.async.ResultCallbackTemplate;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

import static com.google.common.base.Charsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.chomp;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@RequiredArgsConstructor
public class DockerLogger extends ResultCallbackTemplate<DockerLogger, Frame> {
    private final Consumer<String> logEntryConsumer;

    @Override
    public void onNext(Frame object) {
        String entry = chomp(new String(object.getPayload(), UTF_8));
        if (isNotBlank(entry)) {
            logEntryConsumer.accept(entry);
        }
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
