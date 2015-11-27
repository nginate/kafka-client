package com.github.nginate.kafka.network.client;

import com.github.nginate.kafka.exceptions.ConnectionException;
import com.github.nginate.kafka.network.AnswerableMessage;
import com.github.nginate.kafka.network.BinaryMessageDecoder;
import com.github.nginate.kafka.network.BinaryMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class BinaryTcpClient {

    private final Map<Object, CompletableFuture<AnswerableMessage>> responseMap = new ConcurrentHashMap<>();
	private final EventLoopGroup workerGroup;
    private final BinaryTcpClientConfig config;

    private ChannelFuture channelFuture;
	private final Bootstrap bootstrap;
    private volatile boolean connected;

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
						ch.pipeline().addLast(new BinaryMessageDecoder(config.getSerializer()),
								new BinaryMessageEncoder(config.getSerializer()),
                                new BinaryTcpClientHandler(BinaryTcpClient.this));
					}
				});

        bootstrap.validate();
	}

	public void connect() {
        try {
            tryConnect();
        } catch (Exception e) {
            log.error("Not connected to server", e);
            onDisconnect();
        }
    }

    private void tryConnect() {
        channelFuture = bootstrap.connect(config.getHost(), config.getPort()).syncUninterruptibly();
        connected = true;
    }

    public void send(Object message) {
        checkConnection();
        channelFuture.channel().writeAndFlush(message);
	}

    public CompletableFuture<AnswerableMessage> request(AnswerableMessage message) {
        checkConnection();
        CompletableFuture<AnswerableMessage> responseFuture = new CompletableFuture<>();
		responseMap.put(message.getCorrelationId(), responseFuture);
		send(message);
		return responseFuture;
	}

    private void checkConnection() {
        if (!connected) {
            throw new ConnectionException("Connection is not alive");
        }
    }

    void onMessage(Object message) {
        if (message instanceof AnswerableMessage) {
            AnswerableMessage answerableMessage = (AnswerableMessage) message;

            CompletableFuture<AnswerableMessage> responseFuture =
                    responseMap.remove(answerableMessage.getCorrelationId());
            if (responseFuture != null) {
                responseFuture.complete(answerableMessage);
            }
        }
        log.warn("Dead message {}" + message);
	}

    void onDisconnect() {
        connected = false;
        log.info("Reconnecting...");
        channelFuture.channel().eventLoop().schedule(this::tryConnect, 1000, TimeUnit.MILLISECONDS);
    }

	void onException(Throwable cause) {
        log.info("Reconnecting...");
	}

	public void close() {
		channelFuture.channel().closeFuture().syncUninterruptibly();
		workerGroup.shutdownGracefully().syncUninterruptibly();
	}
}
