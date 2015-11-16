package com.github.nginate.kafka.protocol;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiKey {
    ApiKeys value();
}
