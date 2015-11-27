package com.github.nginate.kafka.network;

public interface AnswerableMessage<T> {
	T getCorrelationId();
}
