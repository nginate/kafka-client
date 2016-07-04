package com.github.nginate.kafka.protocol.validation;

public interface ValidatorProvider {
    <T> ResponseValidator<T> validatorFor(T response);
}
