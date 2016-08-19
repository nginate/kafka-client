package com.github.nginate.kafka.core;

import com.github.nginate.kafka.protocol.Error;
import com.github.nginate.kafka.protocol.messages.MessageSet;
import com.github.nginate.kafka.protocol.messages.MessageSet.MessageData;
import com.github.nginate.kafka.protocol.messages.MessageSet.MessageData.Message;
import com.github.nginate.kafka.protocol.messages.dto.Broker;
import com.github.nginate.kafka.protocol.messages.request.HeartbeatRequest;
import com.github.nginate.kafka.protocol.messages.request.JoinGroupRequest;
import com.github.nginate.kafka.protocol.messages.request.ProduceRequest;
import com.github.nginate.kafka.protocol.messages.request.ProduceRequest.ProduceRequestBuilder;
import com.github.nginate.kafka.protocol.messages.request.ProduceRequest.TopicProduceData;
import com.github.nginate.kafka.protocol.messages.request.ProduceRequest.TopicProduceData.PartitionProduceData;
import com.github.nginate.kafka.protocol.messages.response.ListGroupsResponse;
import com.github.nginate.kafka.protocol.validation.ValidatorProvider;
import com.github.nginate.kafka.protocol.validation.ValidatorProviderImpl;
import com.github.nginate.kafka.zookeeper.ZookeeperClient;
import com.github.nginate.kafka.zookeeper.dto.ZkBrokerInfo;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.zip.CRC32;

import static com.github.nginate.kafka.protocol.Error.forCode;
import static java.util.Collections.emptyMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Slf4j
public class KafkaClusterClientImpl implements KafkaClusterClient {
    private final ScheduledExecutorService poller = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService metadataUpdater = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();

    private final ClusterConfiguration configuration;
    private final KafkaSerializer serializer;
    private final ValidatorProvider validatorProvider;
    private final ClusterMetadata metadata;

    private final ZookeeperClient zookeeperClient;
    private final Map<String, List<MessageHandler<?>>> handlers;
    private final Map<String, KafkaDeserializer> deserializers;
    private final Map<String, KafkaBrokerClient> brokerClients; //TODO cache with TTL
    private final ProduceRequestBuilder produceRequestBuilder;
    private ScheduledFuture<?> heartBeatTask;
    private ScheduledFuture<?> metadataTask;

    public KafkaClusterClientImpl() {
        this(ClusterConfiguration.defaultConfig(), new KafkaSerializerImpl());
    }

    public KafkaClusterClientImpl(ClusterConfiguration configuration, KafkaSerializer serializer) {
        this.configuration = configuration;
        this.serializer = serializer;
        validatorProvider = new ValidatorProviderImpl();
        metadata = new ClusterMetadata();
        handlers = new HashMap<>();
        deserializers = new HashMap<>();
        brokerClients = new ConcurrentHashMap<>();
        produceRequestBuilder = ProduceRequest.builder()
                .requiredAcks(configuration.getRequiredAcks())
                .timeout(configuration.getProduceTimeout());

        ZkClient zkClient = new ZkClient(configuration.getZookeeperUrl());
        zookeeperClient = new ZookeeperClient(zkClient);
        metadataTask = metadataUpdater.scheduleAtFixedRate(this::loadClusterMetadata, 0, 1000, TimeUnit.MILLISECONDS);
    }

    private void loadClusterMetadata() {
        List<ZkBrokerInfo> nodes = zookeeperClient.getClusterNodes();
        if (CollectionUtils.isNotEmpty(nodes)) {
            ZkBrokerInfo broker = nodes.get(0);

            String brokerUri = broker.getHost() + broker.getPort();
            KafkaBrokerClient brokerClient = brokerClients.computeIfAbsent(brokerUri,
                    s -> new KafkaBrokerClient(broker.getHost(), broker.getPort()));
            Set<String> topics = metadata.getTopics();
            brokerClient.topicMetadata(topics.toArray(new String[topics.size()])).thenAcceptAsync(metadata::update);
        }
    }

    @Override
    public boolean isClusterOperational() {
        return metadata.availableBrokers() > 0;
    }

    @Synchronized
    @Override
    public <T> void subscribeWith(String topic, KafkaDeserializer<T> deserializer, MessageHandler<T> messageHandler) {
        metadata.addTopic(topic);

        if (handlers.isEmpty()) {
            heartBeatTask = heartbeatExecutor.scheduleAtFixedRate(() -> {

                String memberId = metadata.getMemberId(configuration.getConsumerGroupId());
                if (memberId == null) {
                    Broker coordinator = metadata.coordinator(configuration.getConsumerGroupId());
                    if (coordinator == null) {
                        Map<Broker, CompletableFuture<ListGroupsResponse>> brokerGroups = metadata.clusterNodes()
                                .stream()
                                .collect(toMap(identity(), broker -> getKafkaBrokerClient(broker).listGroups()));

                        log.debug("Requesting consumer groups from {}", brokerGroups.keySet());

                        CompletableFuture
                                .allOf(brokerGroups.values().toArray(new CompletableFuture[brokerGroups.size()]))
                                .thenApply(aVoid -> {
                                    log.debug("All group requests completed");
                                    return brokerGroups.entrySet().stream()
                                            .collect(toMap(Map.Entry::getKey, entry -> entry.getValue().getNow(null)));
                                })
                                .thenApply(map -> {
                                    log.debug("Validating group responses : {}", map);
                                    return map.entrySet().stream().filter(entry -> {
                                        ListGroupsResponse response = entry.getValue();
                                        if (Error.isError(response.getErrorCode())) {
                                            log.warn("List groups request failed : {}", forCode(response
                                                    .getErrorCode()));
                                            return false;
                                        }
                                        return true;
                                    }).collect(toMap(Map.Entry::getKey, entry -> entry.getValue().getGroups()));
                                })
                                .thenAccept(map -> {
                                    log.debug("Updating metadata with new groups {}", map);
                                    metadata.updateGroups(map);
                                });
                    } else {
                        JoinGroupRequest request = JoinGroupRequest.builder()
                                .groupId(configuration.getConsumerGroupId())
                                .build();
                        getKafkaBrokerClient(coordinator).joinGroup(request).thenAccept(response -> {
                            if (Error.isError(response.getErrorCode())) {
                                log.warn("Join group request failed : {}", forCode(response.getErrorCode()));
                            } else {
                                metadata.setMemberData(configuration.getConsumerGroupId(), response);
                            }
                        });
                    }
                } else {
                    HeartbeatRequest request = HeartbeatRequest.builder()
                            .memberId(memberId)
                            .groupId(configuration.getConsumerGroupId())
                            .generationId(metadata.generation(configuration.getConsumerGroupId()))
                            .build();
                    getKafkaBrokerClient(metadata.coordinator(configuration.getConsumerGroupId()))
                            .checkHeartbeat(request)
                            .thenAccept(response -> {
                                log.debug("Heartbeat response {}", response);
                            });
                }
            }, 0, configuration.getPollingInterval(), TimeUnit.MILLISECONDS);
        }

        handlers.putIfAbsent(topic, new ArrayList<>());
        handlers.get(topic).add(messageHandler);
        deserializers.putIfAbsent(topic, deserializer);
        poller.scheduleAtFixedRate(new ConsumeTask<>(() -> handlers.get(topic),
                () -> metadata.brokersForTopic(topic).stream().map(this::getKafkaBrokerClient), deserializer,
                ), 0, configuration.getPollingInterval(), TimeUnit.MILLISECONDS);
    }

    @Synchronized
    @Override
    public void unSubscribeFrom(String topic) {
        handlers.remove(topic);
        //TODO check consumers are gratefully stopped
        if (handlers.isEmpty()) {
            Optional.ofNullable(heartBeatTask).ifPresent(task -> task.cancel(false));
        }
    }

    @Override
    public CompletableFuture<Void> send(String topic, Object message) {
        if (!metadata.topicExists(topic)) {
            metadata.addTopic(topic);
            zookeeperClient.createTopic(topic, configuration.getDefaultPartitions(),
                    configuration.getDefaultReplicationFactor(), emptyMap());
        }

        return metadata.leaderFor(topic)
                .thenApply(this::getKafkaBrokerClient)
                .thenCompose(client -> {
                    byte[] rawKafkaMessage = serializer.serialize(message);

                    Long offset = metadata.offsetForTopic(topic);
                    Integer partition = metadata.partitionForTopic(topic);

                    Message kafkaMessage = buildMessage(rawKafkaMessage);
                    MessageData messageData = buildMessageData(offset, kafkaMessage);
                    PartitionProduceData partitionProduceData = buildPartitionProduceData(partition, messageData);
                    TopicProduceData topicProduceData = buildTopicProduceData(topic, partitionProduceData);
                    ProduceRequest request = buildProduceRequest(topicProduceData);

                    return client.produce(request);
                })
                .thenAccept(produceResponse ->
                        validatorProvider.validatorFor(produceResponse).validate(produceResponse));
    }

    @Override
    public void close() {
        metadata.getTopics().forEach(this::unSubscribeFrom);
        metadataTask.cancel(false);
    }

    private KafkaBrokerClient getKafkaBrokerClient(Broker broker) {
        return brokerClients.computeIfAbsent(broker.getHost() + broker.getPort(), integer ->
                new KafkaBrokerClient(broker.getHost(), broker.getPort()));
    }

    private ProduceRequest buildProduceRequest(TopicProduceData topicProduceData) {
        ProduceRequest request = produceRequestBuilder.build();
        request.setTopicProduceData(new TopicProduceData[]{topicProduceData});
        return request;
    }

    private TopicProduceData buildTopicProduceData(String topic, PartitionProduceData partitionProduceData) {
        return TopicProduceData.builder()
                .topic(topic)
                .partitionProduceData(new PartitionProduceData[]{partitionProduceData})
                .build();
    }

    private PartitionProduceData buildPartitionProduceData(Integer partition, MessageData messageData) {
        MessageSet messageSet = new MessageSet();
        messageSet.setMessageDatas(new MessageData[]{messageData});
        return PartitionProduceData.builder()
                .partition(partition)
                .messageSetSize(1)
                .messageSet(messageSet)
                .build();
    }

    private MessageData buildMessageData(Long offset, Message kafkaMessage) {
        return MessageData.builder()
                .offset(offset)
                .messageSize(kafkaMessage.getValue().length)
                .message(kafkaMessage)
                .build();
    }

    private Message buildMessage(byte[] rawKafkaMessage) {
        CRC32 crc32 = new CRC32();
        crc32.update(rawKafkaMessage);
        long messageCRC = crc32.getValue();

        return Message.builder()
                .attributes((byte) 0)
                .crc((int) messageCRC)
                .magicByte((byte) 0)
                .value(rawKafkaMessage)
                .build();
    }
}
