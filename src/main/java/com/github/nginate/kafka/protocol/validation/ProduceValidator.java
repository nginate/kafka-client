package com.github.nginate.kafka.protocol.validation;

import com.github.nginate.kafka.exceptions.KafkaException;
import com.github.nginate.kafka.exceptions.ProduceException;
import com.github.nginate.kafka.exceptions.QuotaViolationException;
import com.github.nginate.kafka.protocol.Error;
import com.github.nginate.kafka.protocol.messages.response.ProduceResponse;
import com.github.nginate.kafka.protocol.messages.response.ProduceResponse.ProduceResponseData;
import com.github.nginate.kafka.protocol.messages.response.ProduceResponse.ProduceResponseData
        .ProduceResponsePartitionData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

public class ProduceValidator implements ResponseValidator<ProduceResponse> {
    @Override
    public void validate(ProduceResponse response) throws KafkaException {
        if (response.getThrottleTimeMs() > 0) {
            throw new QuotaViolationException(response.getThrottleTimeMs());
        }
        Map<String, List<Error>> topicErrors = new HashMap<>();

        for (ProduceResponseData data : response.getProduceResponseData()) {
            List<Error> topicExceptions = stream(data.getProduceResponsePartitionData())
                    .map(ProduceResponsePartitionData::getErrorCode)
                    .filter(Error::isError)
                    .map(Error::forCode)
                    .collect(Collectors.toList());

            if (!topicExceptions.isEmpty()) {
                topicErrors.put(data.getTopic(), topicExceptions);
            }
        }

        if (!topicErrors.isEmpty()) {
            throw new ProduceException(topicErrors);
        }
    }
}
