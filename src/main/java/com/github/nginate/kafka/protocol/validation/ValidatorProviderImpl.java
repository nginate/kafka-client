package com.github.nginate.kafka.protocol.validation;

import com.github.nginate.kafka.exceptions.KafkaException;
import com.github.nginate.kafka.protocol.messages.response.ProduceResponse;

import java.util.HashMap;
import java.util.Map;

public class ValidatorProviderImpl implements ValidatorProvider {
    private final Map<Class<?>, ResponseValidator<?>> validators = new HashMap<>();

    public ValidatorProviderImpl() {
        validators.put(ProduceResponse.class, new ProduceValidator());
    }

    @Override
    public <T> ResponseValidator<T> validatorFor(T response) {
        //noinspection unchecked
        return (ResponseValidator<T>) validators.entrySet().stream()
                .filter(entry -> entry.getKey().isAssignableFrom(response.getClass()))
                .findAny().map(Map.Entry::getValue).orElseThrow(() ->
                        new KafkaException("Provider does not have validator for type " + response.getClass()));
    }
}
