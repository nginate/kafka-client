package com.github.nginate.kafka.util;

import lombok.experimental.UtilityClass;
import org.slf4j.helpers.MessageFormatter;

@UtilityClass
public final class StringUtils {

    public static String format(String template, Object... args) {
        return MessageFormatter.arrayFormat(template, args).getMessage();
    }
}
