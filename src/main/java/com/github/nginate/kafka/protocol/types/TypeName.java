package com.github.nginate.kafka.protocol.types;

public enum TypeName {
    /**
     *  Signed integer (1 byte) with the given precision (in bits) stored in big endian order.
     */
    INT8,
    /**
     *  Signed integer (2 bytes) with the given precision (in bits) stored in big endian order.
     */
    INT16,
    /**
     *  Signed integer (4 bytes) with the given precision (in bits) stored in big endian order.
     */
    INT32,
    /**
     *  Signed integer (8 bytes) with the given precision (in bits) stored in big endian order.
     */
    INT64,
    /**
     * Consist of a signed int16 giving a length N followed by N bytes of content. A length of -1 indicates null.
     */
    STRING,
    /**
     * Consist of a signed int32 giving a length N followed by N bytes of content. A length of -1 indicates null.
     */
    BYTES
}
