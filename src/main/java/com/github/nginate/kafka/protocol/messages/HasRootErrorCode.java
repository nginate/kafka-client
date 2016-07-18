package com.github.nginate.kafka.protocol.messages;

import static com.github.nginate.kafka.protocol.Error.isError;

/**
 * Some of kafka protocol responses do not have single error code at the top of dto structure - mostly b/c they contain arrays of
 * sub responses (like in produce when you can send multiple messages - each message could cause its own error). But most of them do have single
 * error code and that's why they could be grouped to simplify error handling. This interface is a marker for such responses.
 */
public interface HasRootErrorCode {
    /**
     * Get root error code of this response
     *
     * @return error code
     * @see com.github.nginate.kafka.protocol.Error
     */
    Short getErrorCode();

    /**
     * Check if server responded with error
     *
     * @return true if error code field in not null and contains non-error code value
     */
    default Boolean isSuccessful() {
        Short code = getErrorCode();
        return code != null && !isError(code.intValue());
    }
}
