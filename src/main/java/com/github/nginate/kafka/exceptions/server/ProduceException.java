package com.github.nginate.kafka.exceptions.server;

import com.github.nginate.kafka.exceptions.AbstractServerException;
import com.github.nginate.kafka.protocol.Error;

import java.util.List;
import java.util.Map;

import static com.github.nginate.kafka.util.StringUtils.format;

public class ProduceException extends AbstractServerException {

    private final Map<String, List<Error>> errors;

    public ProduceException(Map<String, List<Error>> errors) {
        super(format("Produce request failed with following errors : {}", errors));
        this.errors = errors;
    }
}
