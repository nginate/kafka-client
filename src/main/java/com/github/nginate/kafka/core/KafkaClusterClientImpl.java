package com.github.nginate.kafka.core;

import com.github.nginate.kafka.protocol.Error;
import com.github.nginate.kafka.protocol.messages.MessageSet;
import com.github.nginate.kafka.protocol.messages.MessageSet.MessageData;
import com.github.nginate.kafka.protocol.messages.MessageSet.MessageData.Message;
import com.github.nginate.kafka.protocol.messages.request.JoinGroupRequest;
import com.github.nginate.kafka.protocol.messages.request.ProduceRequest;
import com.github.nginate.kafka.protocol.messages.request.ProduceRequest.ProduceRequestBuilder;
import com.github.nginate.kafka.protocol.messages.request.ProduceRequest.TopicProduceData;
import com.github.nginate.kafka.protocol.messages.request.ProduceRequest.TopicProduceData.PartitionProduceData;
import com.github.nginate.kafka.protocol.messages.response.GroupCoordinatorResponse.GroupCoordinatorBroker;
import com.github.nginate.kafka.protocol.messages.response.TopicMetadataResponse.TopicMetadataBroker;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.CRC32;

import static java.util.Collections.emptyMap;

@Slf4j
public class KafkaClusterClientImpl implements KafkaClusterClient {
    private final ScheduledExecutorService poller = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService metadataUpdater = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();

    private final ClusterConfiguration configuration;
    private final KafkaSerializer serializer;
    private final ValidatorProvider validatorProvider;
    private final ClusterMetadata metadata;
    private final SubscriberContext subscriberContext;

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
        subscriberContext = new SubscriberContext();
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
                GroupCoordinatorBroker coordinator = metadata.computeCoordinatorIfAbsent(topic, () -> {
                    Optional<TopicMetadataBroker> randomBroker = metadata.randomBroker();
                    AtomicReference<GroupCoordinatorBroker> coordinatorRef = new AtomicReference<>();
                    if (randomBroker.isPresent()) {
                        KafkaBrokerClient client = getKafkaBrokerClient(randomBroker.get());
                        client.getGroupCoordinator(configuration.getConsumerGroupId())
                                .whenComplete((response, throwable) -> {
                                    if (response != null) {
                                        short errorCode = response.getErrorCode();
                                        if (!Error.isError(errorCode)) {
                                            coordinatorRef.set(response.getCoordinator());
                                            //TODO schedule heartbeats
                                        }
                                    }
                                });
                    }

                    return coordinatorRef.get();
                });

                metadata.checkGroupJoined(configuration.getConsumerGroupId(), () -> {
                    KafkaBrokerClient coordinatorClient = getKafkaBrokerClient(coordinator);
                    AtomicReference<String> memberIdRef = new AtomicReference<>(null);

                    JoinGroupRequest request = JoinGroupRequest.builder().build();
                    coordinatorClient.joinGroup(request).whenComplete((response, throwable) -> {
                        if (response != null) {
                            short errorCode = response.getErrorCode();
                            if (!Error.isError(errorCode)) {
                                memberIdRef.set(response.getMemberId());
                                //TODO find out what to do with other data
                            }
                        }
                    });

                    return memberIdRef.get();
                });
            }, 0, configuration.getPollingInterval(), TimeUnit.MILLISECONDS);
        }

        handlers.putIfAbsent(topic, new ArrayList<>());
        handlers.get(topic).add(messageHandler);
        deserializers.putIfAbsent(topic, deserializer);
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

    private KafkaBrokerClient getKafkaBrokerClient(TopicMetadataBroker broker) {
        return brokerClients.computeIfAbsent(broker.getHost() + broker.getPort(), integer ->
                new KafkaBrokerClient(broker.getHost(), broker.getPort()));
    }

    private KafkaBrokerClient getKafkaBrokerClient(GroupCoordinatorBroker broker) {
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
