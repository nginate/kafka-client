package com.github.nginate.kafka.serialization;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Type {
    TypeName value();

    int order() default 0;
}
