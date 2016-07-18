package com.github.nginate.kafka.exceptions.server;

import com.github.nginate.kafka.exceptions.AbstractServerException;
import com.github.nginate.kafka.protocol.Error;
import com.github.nginate.kafka.protocol.messages.HasRootErrorCode;

import static com.github.nginate.kafka.util.StringUtils.format;

public class GenericServerException extends AbstractServerException {
    public GenericServerException(HasRootErrorCode response, Error error) {
        super(format("Received response {} with following error : {}", response, error));
    }
}
