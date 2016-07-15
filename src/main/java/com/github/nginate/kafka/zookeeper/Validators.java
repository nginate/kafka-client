package com.github.nginate.kafka.zookeeper;

import com.github.nginate.kafka.zookeeper.exception.ZookeeperValidationException;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.nginate.kafka.util.StringUtils.format;

@UtilityClass
public class Validators {
    private static final Pattern LEGAL_CHARS = Pattern.compile("^[a-zA-Z0-9\\._\\-]+$");
    private static final int MAX_NAME_LENGTH = 249;

    public static void validateTopic(String topic) {
        int length = StringUtils.length(topic);
        if (length < 1 || length > MAX_NAME_LENGTH) {
            throw new ZookeeperValidationException("Topic name should be between 1 and 249 symbols");
        }
        if (".".equals(topic) || "..".equals(topic)) {
            throw new ZookeeperValidationException(". or .. is not a valida name for topic");
        }
        Matcher matcher = LEGAL_CHARS.matcher(topic);
        if (!matcher.find()) {
            throw new ZookeeperValidationException("Topic name should contain only a-zA-Z0-9._- symbols");
        }

    }

    public static boolean topicHasCollisionChars(String topic) {
        return StringUtils.containsAny(topic, ".", "_");
    }

    public static void validateTopicConfig(Map<String, String> config) {
        if (config.containsKey(null)) {
            throw new ZookeeperValidationException("Topic config can't contain null keys");
        }
        config.entrySet().stream().filter(entry -> entry.getValue() == null).findAny()
                .ifPresent(entry -> {
                    throw new ZookeeperValidationException(format("Topic config entry {} is null", entry.getKey()));
                });
        config.entrySet().stream().filter(entry -> !TopicConfigEntry.isValidEntry(entry.getKey(), entry.getValue()))
                .findAny().ifPresent(invalidEntry -> {
            throw new ZookeeperValidationException(format("Topic config entry {} can't have value {}",
                    invalidEntry.getKey(), invalidEntry.getValue()));
        });
    }
}
