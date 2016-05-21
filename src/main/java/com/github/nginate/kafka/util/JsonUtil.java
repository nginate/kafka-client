package com.github.nginate.kafka.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.nginate.kafka.exceptions.SerializationException;
import lombok.experimental.UtilityClass;

import java.io.IOException;

@UtilityClass
public class JsonUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        MAPPER.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        MAPPER.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
        MAPPER.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        MAPPER.setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.NONE);
        MAPPER.setConfig(MAPPER.getSerializationConfig().without(SerializationFeature.FAIL_ON_EMPTY_BEANS));
        MAPPER.setConfig(MAPPER.getDeserializationConfig().without(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES));
    }

    public static String toJson(Object obj) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new SerializationException(e.getMessage(), e);
        }
    }

    public static <T> T fromJson(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (IOException e) {
            throw new SerializationException(e.getMessage(), e);
        }
    }
}
