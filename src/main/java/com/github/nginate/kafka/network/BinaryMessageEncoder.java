package com.github.nginate.kafka.network;


import com.github.nginate.kafka.exceptions.SerializationException;
import com.github.nginate.kafka.serialization.BinaryMessageSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class BinaryMessageEncoder extends MessageToByteEncoder<AnswerableMessage> {
	private final BinaryMessageSerializer serializer;

	@Override
	protected void encode(ChannelHandlerContext ctx, AnswerableMessage msg, ByteBuf out) throws Exception {
		try {
			serializer.serialize(out, msg);
		} catch (SerializationException e) {
			log.error("Serialization error", e);
		}
	}
}
