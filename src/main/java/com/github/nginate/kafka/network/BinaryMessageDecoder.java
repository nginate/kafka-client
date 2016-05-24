package com.github.nginate.kafka.network;

import com.github.nginate.kafka.exceptions.SerializationException;
import com.github.nginate.kafka.network.client.BinaryClientContext;
import com.github.nginate.kafka.serialization.BinaryMessageSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class BinaryMessageDecoder extends ReplayingDecoder<Void> {
	private final BinaryMessageSerializer serializer;
	private final BinaryClientContext clientContext;

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		try {
			out.add(serializer.deserialize(in, clientContext));
		} catch (SerializationException e) {
			log.error("Deserialization error", e);
		}
	}
}
