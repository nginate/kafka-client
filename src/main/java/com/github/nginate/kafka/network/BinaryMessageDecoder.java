package com.github.nginate.kafka.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class BinaryMessageDecoder extends ReplayingDecoder<Void> {
	private final BinaryMessageSerializer serializer;

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		out.add(serializer.deserialize(in));
	}
}
