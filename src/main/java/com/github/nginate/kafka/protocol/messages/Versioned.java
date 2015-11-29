package com.github.nginate.kafka.protocol.messages;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Versioned {
    /**
     * Shows which API versions uses marked field
     * @return an array with version numbers
     */
    ApiVersion[] includesVersions() default {};

    /**
     * Sometimes it's easier to exclude single unsupported API than enumerating all supported
     * @return an array with unsupported version numbers
     */
    ApiVersion[] excludesVersions() default {};

    /**
     * If a field was introduced at some point and was not removed till some API version
     * @return API version when field was introduced (inclusive)
     */
    ApiVersion since() default ApiVersion.V0;

    /**
     * If a field existed only till some defined API version
     * @return API version when field was removed (exclusive)
     */
    ApiVersion till() default ApiVersion.UNDEFINED;
}
