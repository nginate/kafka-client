package com.github.nginate.kafka.core;

import com.github.nginate.kafka.protocol.messages.MessageSet;
import com.github.nginate.kafka.protocol.messages.MessageSet.MessageData;
import com.github.nginate.kafka.protocol.messages.MessageSet.MessageData.Message;
import com.github.nginate.kafka.protocol.messages.dto.Broker;
import com.github.nginate.kafka.protocol.messages.request.ProduceRequest;
import com.github.nginate.kafka.protocol.messages.request.ProduceRequest.ProduceRequestBuilder;
import com.github.nginate.kafka.protocol.messages.request.ProduceRequest.TopicProduceData;
import com.github.nginate.kafka.protocol.messages.request.ProduceRequest.TopicProduceData.PartitionProduceData;
import com.github.nginate.kafka.zookeeper.ZkBrokerInfo;
import com.github.nginate.kafka.zookeeper.ZookeeperClient;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;

@Slf4j
public class KafkaClusterClientImpl implements KafkaClusterClient {
    private final ClusterConfiguration configuration;
    private final KafkaSerializer serializer;
    private final ClusterMetadata metadata;

    private final ZookeeperClient zookeeperClient;
    private final Map<String, List<MessageHandler<?>>> handlers;
    private final Map<String, KafkaDeserializer> deserializers;
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
        boolean isCleanStart = handlers.isEmpty();

        handlers.putIfAbsent(topic, new ArrayList<>());
        handlers.get(topic).add(messageHandler);
        deserializers.put(topic, deserializer);

        if (isCleanStart) {
            // TODO start consumer scheduler
        }
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
    public void send(String topic, Object message) {
        byte[] rawKafkaMessage = serializer.serialize(message);

        Message kafkaMessage = buildMessage(rawKafkaMessage);
        MessageData messageData = buildMessageData(kafkaMessage);
        PartitionProduceData partitionProduceData = buildPartitionProduceData(messageData);
        TopicProduceData topicProduceData = buildTopicProduceData(topic, partitionProduceData);
        ProduceRequest request = buildProduceRequest(topicProduceData);

        log.debug("Sending produce request : {}", request);

        //TODO find leader to send message
    }

    private ProduceRequest buildProduceRequest(TopicProduceData topicProduceData) {
        TopicProduceData[] topicProduceDatas = {topicProduceData};

        ProduceRequest request = produceRequestBuilder.build();
        request.setTopicProduceData(topicProduceDatas);
        return request;
    }

    private TopicProduceData buildTopicProduceData(String topic, PartitionProduceData partitionProduceData) {
        PartitionProduceData[] partitionProduceDatas = {partitionProduceData};

        return TopicProduceData.builder()
                .topic(topic)
                .partitionProduceData(partitionProduceDatas)
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
}
