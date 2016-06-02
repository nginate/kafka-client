package com.github.nginate.kafka.network.client;

import com.github.nginate.kafka.exceptions.ConnectionException;
import com.github.nginate.kafka.network.AnswerableMessage;
import com.github.nginate.kafka.network.BinaryMessageDecoder;
import com.github.nginate.kafka.network.BinaryMessageEncoder;
import com.github.nginate.kafka.network.FutureNotifier;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
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
    private final Bootstrap bootstrap;
    private final BinaryClientContext context = new BinaryClientContext();

    private volatile ChannelFuture channelFuture;
    private volatile AtomicBoolean connected = new AtomicBoolean();

	public BinaryTcpClient(BinaryTcpClientConfig config) {
        bootstrap = new Bootstrap()
                .remoteAddress(config.getHost(), config.getPort())
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
				.option(ChannelOption.SO_KEEPALIVE, true)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectionTimeoutMillis())
				.option(ChannelOption.SO_TIMEOUT, config.getSocketTimeoutMillis())
                .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(config.getMaxMessagesToRead()))
                .handler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                                new BinaryMessageDecoder(config.getSerializer(), context),
                                new BinaryMessageEncoder(config.getSerializer()),
                                new BinaryTcpClientHandler(BinaryTcpClient.this)
                        );
                    }
                })
                .validate();

        connect();
    }

    public boolean isConnectionAlive() {
        return channelFuture.channel().isActive();
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

    public CompletableFuture<Void> close() {
        if (!connected.compareAndSet(true, false)) {
            return CompletableFuture.completedFuture(null);
        }

        return waitForCompletion(bootstrap.config().group().shutdownGracefully())
                .whenComplete((o, throwable) -> {
                    responseMap.clear();
                    context.clear();
                });
    }

    private CompletableFuture<Void> waitForCompletion(Future<?> future) {
        CompletableFuture<Object> promise = new CompletableFuture<>();
        future.addListener(new FutureNotifier<>(promise));
        return promise.thenApply(o -> (Void) null);
    }

    private void connect() {
        if (connected.compareAndSet(false, true)) {
            tryConnect();
        }
    }

    private void tryConnect() {
        channelFuture = bootstrap.connect().addListener(future -> {
            if (!future.isSuccess()) {
                onDisconnect();
            }
        });
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
        if (connected.get()) {
            log.info("Reconnecting...");
            channelFuture.channel().eventLoop().schedule(this::tryConnect, 1000, TimeUnit.MILLISECONDS);
        }
    }

	void onException(Throwable cause) {
        log.error("Unexpected exception from channel", cause);
    }
}
