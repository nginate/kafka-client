package com.github.nginate.kafka.network;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BinaryMessageEncoder extends MessageToByteEncoder<AnswerableMessage> {
	private final BinaryMessageSerializer serializer;

	@Override
	protected void encode(ChannelHandlerContext ctx, AnswerableMessage msg, ByteBuf out) throws Exception {
		serializer.serialize(out, msg);
	}
}
