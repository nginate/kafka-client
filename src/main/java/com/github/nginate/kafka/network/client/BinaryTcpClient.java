package com.github.nginate.kafka.network.client;

import com.github.nginate.kafka.exceptions.ConnectionException;
import com.github.nginate.kafka.network.AnswerableMessage;
import com.github.nginate.kafka.network.BinaryMessageDecoder;
import com.github.nginate.kafka.network.BinaryMessageEncoder;
import com.google.common.base.Throwables;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@ThreadSafe
public class BinaryTcpClient {

    private final Map<Object, CompletableFuture<AnswerableMessage>> responseMap = new ConcurrentHashMap<>();
	private final EventLoopGroup workerGroup;
    private final BinaryTcpClientConfig config;
    private final BinaryClientContext context = new BinaryClientContext();

    private ChannelFuture channelFuture;
	private final Bootstrap bootstrap;
    private volatile AtomicBoolean connected = new AtomicBoolean();

	public BinaryTcpClient(BinaryTcpClientConfig config) {
        this.config = config;
        workerGroup = new NioEventLoopGroup();

        bootstrap = new Bootstrap()
				.group(workerGroup)
				.channel(NioSocketChannel.class)
				.option(ChannelOption.SO_KEEPALIVE, true)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectionTimeoutMillis())
				.option(ChannelOption.SO_TIMEOUT, config.getSocketTimeoutMillis())
				.option(ChannelOption.MAX_MESSAGES_PER_READ, config.getMaxMessagesToRead())
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new BinaryMessageDecoder(config.getSerializer(), context),
                                new BinaryMessageEncoder(config.getSerializer()),
                                new BinaryTcpClientHandler(BinaryTcpClient.this));
					}
				});

        bootstrap.validate();
	}

	public void connect() {
        if (connected.compareAndSet(false, true)) {
            tryConnect();
        }
    }

    public void send(Object message) {
        checkConnection();
        channelFuture.channel().writeAndFlush(message);
	}

    public <T> CompletableFuture<T> request(AnswerableMessage message, Class<T> responseType,
                                            BinaryMessageMetadata messageMetadata) {
        checkConnection();
        CompletableFuture<AnswerableMessage> responseFuture = new CompletableFuture<>();
        context.addMetadata(message.getCorrelationId(), messageMetadata);
        responseMap.put(message.getCorrelationId(), responseFuture);
		send(message);
        return responseFuture.thenApplyAsync(responseType::cast);
    }

    private void tryConnect() {
        channelFuture = bootstrap.connect(config.getHost(), config.getPort());
        channelFuture.syncUninterruptibly();
    }

    private void checkConnection() {
        if (!connected.get()) {
            throw new ConnectionException("Connection is not alive");
        }
    }

    void onMessage(Object message) {
        if (message instanceof AnswerableMessage) {
            AnswerableMessage answerableMessage = (AnswerableMessage) message;

            context.removeMetadata(answerableMessage.getCorrelationId());
            CompletableFuture<AnswerableMessage> responseFuture =
                    responseMap.remove(answerableMessage.getCorrelationId());
            if (responseFuture != null) {
                responseFuture.complete(answerableMessage);
            } else {
                log.warn("Dead message {}", message);
            }
        } else {
            log.warn("Wrong message {}", message);
        }
	}

    void onDisconnect() {
        log.info("Reconnecting...");
        channelFuture.channel().eventLoop().schedule(this::tryConnect, 1000, TimeUnit.MILLISECONDS);
    }

	void onException(Throwable cause) {
        log.error("Unexpected exception from channel", cause);
    }

	public void close() {
        try {
            responseMap.clear();
            context.clear();
            channelFuture.channel().close().sync().await(1000, TimeUnit.MILLISECONDS);
            workerGroup.shutdownGracefully().sync().await(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw Throwables.propagate(e);
        }
    }
}
