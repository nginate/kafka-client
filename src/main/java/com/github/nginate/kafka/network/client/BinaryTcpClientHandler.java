package com.github.nginate.kafka.network.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class BinaryTcpClientHandler extends ChannelInboundHandlerAdapter {
    private final BinaryTcpClient client;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (log.isTraceEnabled()) {
            log.trace("Received message {}", msg);
        }
        log.debug("Received message {}", msg);
        client.onMessage(msg);
	}

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Connected to {}", ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Disconnected from {}", ctx.channel().remoteAddress());
        client.onDisconnect();
        super.channelInactive(ctx);
    }

    @Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        client.onException(cause);
	}
}
