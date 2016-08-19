package com.github.nginate.kafka.core;

import com.github.nginate.kafka.protocol.Error;
import com.github.nginate.kafka.protocol.messages.MessageSet;
import com.github.nginate.kafka.protocol.messages.MessageSet.MessageData;
import com.github.nginate.kafka.protocol.messages.request.FetchRequest;
import com.github.nginate.kafka.protocol.messages.request.FetchRequest.FetchRequestTopicData;
import com.github.nginate.kafka.protocol.messages.request.FetchRequest.FetchRequestTopicData.FetchRequestPartitionData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Arrays.stream;

@Slf4j
@RequiredArgsConstructor
public class ConsumeTask<T> implements Runnable {

    private final Supplier<List<MessageHandler<?>>> handlers;
    private final Supplier<Stream<KafkaBrokerClient>> clients;
    private final KafkaDeserializer<T> deserializer;
    private final SubscriberContext context;
    private final String topic;
    private final Integer partition;

    @Override
    public void run() {
        FetchRequestPartitionData partitionData = FetchRequestPartitionData.builder()
                .fetchOffset(context.getPosition())
                .maxBytes(context.getMaxBytes())
                .partition(partition)
                .build();

        FetchRequestTopicData topicData = FetchRequestTopicData.builder()
                .topic(topic)
                .partitionData(ArrayUtils.toArray(partitionData))
                .build();

        FetchRequest request = FetchRequest.builder()
                .maxWaitTime(context.getMaxWaitMillis())
                .minBytes(context.getMinBytes())
                .replicaId(-1) // common consumer
                .topicData(ArrayUtils.toArray(topicData))
                .build();

        clients.get().findAny().ifPresent(client -> client.fetch(request)
                .thenAccept(response -> {
                    stream(response.getTopicData()).flatMap(data -> stream(data.getPartitionData()))
                            .forEach(responsePartitionData -> {
                                Short errorCode = responsePartitionData.getErrorCode();
                                if (!Error.isError(errorCode)) {
                                    MessageSet messageSet = responsePartitionData.getMessageSet();
                                    for (MessageData messageData : messageSet.getMessageDatas()) {
                                        log.debug("Message data consumed {}", messageData);
                                        byte[] value = messageData.getMessage().getValue();
                                        T message = deserializer.deserialize(value);

                                        List<MessageHandler<?>> messageHandlers = handlers.get();
                                        for (MessageHandler<?> messageHandler : messageHandlers) {
                                            messageHandler.onMessage(message);
                                        }

                                        context.updateOffset(1);
                                    }
                                } else {
                                    log.warn("Consume operation failed : {}", Error.forCode(errorCode));
                                }
                            });
                }));
    }
}
