package com.github.nginate.kafka.serialization;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiKey {
    int value();
}
