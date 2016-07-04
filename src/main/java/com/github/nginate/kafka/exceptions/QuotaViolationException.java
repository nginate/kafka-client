package com.github.nginate.kafka.exceptions;

import lombok.Getter;

import static com.github.nginate.kafka.util.StringUtils.format;

public class QuotaViolationException extends KafkaException {
    /**
     * Duration in milliseconds for which the request was throttled due to quota violation.
     */
    @Getter
    private final int millis;

    public QuotaViolationException(int millis) {
        super(format("Request was throttled due to quota violation for {} ms", millis));
        this.millis = millis;
    }
}
