package com.github.nginate.kafka.protocol.validation;

import com.github.nginate.kafka.exceptions.KafkaException;
import com.github.nginate.kafka.exceptions.server.GenericServerException;
import com.github.nginate.kafka.protocol.Error;
import com.github.nginate.kafka.protocol.messages.HasRootErrorCode;

public class HasRootErrorCodeValidator implements ResponseValidator<HasRootErrorCode> {
    @Override
    public void validate(HasRootErrorCode response) throws KafkaException {
        if (!response.isSuccessful()) {
            throw new GenericServerException(response, Error.forCode(response.getErrorCode()));
        }
    }
}
