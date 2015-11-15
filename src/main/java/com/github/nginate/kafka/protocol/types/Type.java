package com.github.nginate.kafka.protocol.types;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Type {
    TypeName value();
    boolean repeatable() default false;
}
