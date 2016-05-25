package com.github.nginate.kafka.core;

import com.github.nginate.kafka.exceptions.KafkaException;
import com.github.nginate.kafka.protocol.ErrorCodes;
import com.github.nginate.kafka.protocol.messages.MessageSet;
import com.github.nginate.kafka.protocol.messages.MessageSet.MessageData;
import com.github.nginate.kafka.protocol.messages.MessageSet.MessageData.Message;
import com.github.nginate.kafka.protocol.messages.dto.Broker;
import com.github.nginate.kafka.protocol.messages.request.JoinGroupRequest;
import com.github.nginate.kafka.protocol.messages.request.ProduceRequest;
import com.github.nginate.kafka.protocol.messages.request.ProduceRequest.ProduceRequestBuilder;
import com.github.nginate.kafka.protocol.messages.request.ProduceRequest.TopicProduceData;
import com.github.nginate.kafka.protocol.messages.request.ProduceRequest.TopicProduceData.PartitionProduceData;
import com.github.nginate.kafka.protocol.messages.response.ProduceResponse;
import com.github.nginate.kafka.zookeeper.ZkBrokerInfo;
import com.github.nginate.kafka.zookeeper.ZookeeperClient;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.CRC32;

import static com.github.nginate.commons.lang.await.Await.waitUntil;
import static com.github.nginate.kafka.util.StringUtils.format;
import static java.util.Arrays.stream;

@Slf4j
public class KafkaClusterClientImpl implements KafkaClusterClient {
    private final ScheduledExecutorService poller = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();

    private final ClusterConfiguration configuration;
    private final KafkaSerializer serializer;
    private final ClusterMetadata metadata;

    private final ZookeeperClient zookeeperClient;
    private final Map<String, List<MessageHandler<?>>> handlers;
    private final Map<String, KafkaDeserializer> deserializers;
    private final Map<Integer, KafkaBrokerClient> brokerClients; //TODO cache with TTL
    private final Map<String, ScheduledFuture<?>> pollingTasks = new HashMap<>();
    private final ProduceRequestBuilder produceRequestBuilder;

    public KafkaClusterClientImpl() {
        this(ClusterConfiguration.defaultConfig(), new KafkaSerializerImpl());
    }

    public KafkaClusterClientImpl(ClusterConfiguration configuration, KafkaSerializer serializer) {
        this.configuration = configuration;
        this.serializer = serializer;
        metadata = new ClusterMetadata();
        handlers = new HashMap<>();
        deserializers = new HashMap<>();
        brokerClients = new ConcurrentHashMap<>();
        produceRequestBuilder = ProduceRequest.builder()
                .requiredAcks(configuration.getRequiredAcks())
                .timeout(configuration.getProduceTimeout());

        ZkClient zkClient = new ZkClient(configuration.getZookeeperUrl());
        zookeeperClient = new ZookeeperClient(zkClient);
        loadClusterMetadata();
    }

    private void loadClusterMetadata() {
        List<ZkBrokerInfo> nodes = zookeeperClient.getClusterNodes();
        if (CollectionUtils.isNotEmpty(nodes)) {
            ZkBrokerInfo broker = nodes.get(0);

            KafkaBrokerClient brokerClient = new KafkaBrokerClient(broker.getHost(), broker.getPort());
            brokerClient.topicMetadata().thenAcceptAsync(metadata::update).thenAccept(aVoid -> brokerClient.close());
        }
    }

    @Synchronized
    @Override
    public <T> void subscribeWith(String topic, KafkaDeserializer<T> deserializer, MessageHandler<T> messageHandler) {
        handlers.putIfAbsent(topic, new ArrayList<>());
        handlers.get(topic).add(messageHandler);
        deserializers.put(topic, deserializer);

        metadata.initTopicLog(topic, configuration.getDefaultGeneration());

        ScheduledFuture<?> scheduledFuture = poller.scheduleAtFixedRate(() -> {
            Broker coordinator = metadata.computeCoordinatorIfAbsent(topic, () -> {
                Broker randomBroker = metadata.randomBroker();
                AtomicReference<Broker> coordinatorRef = new AtomicReference<>(null);
                if (randomBroker != null) {
                    KafkaBrokerClient client = getKafkaBrokerClient(randomBroker);
                    client.getGroupCoordinator(configuration.getConsumerGroupId())
                            .whenComplete((response, throwable) -> {
                                if (response != null) {
                                    short errorCode = response.getErrorCode();
                                    if (errorCode == ErrorCodes.NO_ERROR) {
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
                        if (errorCode == ErrorCodes.NO_ERROR) {
                            memberIdRef.set(response.getMemberId());
                            //TODO find out what to do with other data
                        }
                    }
                });

                return memberIdRef.get();
            });

        }, 0, configuration.getPollingInterval(), TimeUnit.MILLISECONDS);
        pollingTasks.put(topic, scheduledFuture);
    }

    @Synchronized
    @Override
    public void unSubscribeFrom(String topic) {
        handlers.remove(topic);
        //TODO check consumers are gratefully stopped
        if (handlers.isEmpty()) {
            //TODO stop consuming
        }
    }

    @Override
    public CompletableFuture<Void> send(String topic, Object message) {
        return CompletableFuture.runAsync(() ->
                waitUntil(10000, () -> metadata.leaderFor(topic).orElse(metadata.randomBroker()) != null))
                .thenRun(() -> {
                    byte[] rawKafkaMessage = serializer.serialize(message);

                    Message kafkaMessage = buildMessage(rawKafkaMessage);
                    MessageData messageData = buildMessageData(kafkaMessage);
                    PartitionProduceData partitionProduceData = buildPartitionProduceData(messageData);
                    TopicProduceData topicProduceData = buildTopicProduceData(topic, partitionProduceData);
                    ProduceRequest request = buildProduceRequest(topicProduceData);

                    Broker broker = metadata.leaderFor(topic).orElse(metadata.randomBroker());
                    log.debug("Sending produce request : {} to {}", request, broker);

                    KafkaBrokerClient topicLeaderClient = getKafkaBrokerClient(broker);

                    topicLeaderClient.produce(request)
                            .thenAccept(produceResponse -> {
                                if (isProduceFailed(produceResponse)) {
                                    throw new KafkaException(format("Produce request failed : {}", produceResponse));
                                }
                            });
                });
    }

    private KafkaBrokerClient getKafkaBrokerClient(Broker broker) {
        return brokerClients.computeIfAbsent(broker.getNodeId(), integer ->
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

    private PartitionProduceData buildPartitionProduceData(MessageData messageData) {
        MessageSet messageSet = new MessageSet();
        messageSet.setMessageDatas(new MessageData[]{messageData});
        return PartitionProduceData.builder()
                .partition(0) //FIXME select actual partition
                .messageSetSize(1)
                .messageSet(messageSet)
                .build();
    }

    private MessageData buildMessageData(Message kafkaMessage) {
        return MessageData.builder()
                .offset(0L)
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

    private boolean isProduceFailed(ProduceResponse response) {
        return stream(response.getProduceResponseData())
                .flatMap(data -> stream(data.getProduceResponsePartitionData()))
                .filter(produceResponsePartitionData -> produceResponsePartitionData.getErrorCode() != -1)
                .findAny()
                .isPresent();
    }
}
