package com.github.nginate.kafka.protocol.messages;

import com.github.nginate.kafka.protocol.types.Type;
import lombok.Data;

import static com.github.nginate.kafka.protocol.types.TypeName.*;

@Data
public class MessageSet {
    @Type(WRAPPER)
    private MessageData[] messageData;

    @Data
    public static class MessageData {
        /**
         * This is the offset used in kafka as the log sequence number. When the producer is sending messages it doesn't
         * actually know the offset and can fill in any value here it likes.
         */
        @Type(INT64)
        private Long offset;
        @Type(value = INT32, order = 1)
        private Integer messageSize;
        @Type(value = WRAPPER, order = 2)
        private Message message;

        @Data
        public static class Message {
            /**
             * The CRC is the CRC32 of the remainder of the message bytes. This is used to check the integrity of
             * the message on the broker and consumer.
             */
            @Type(INT32)
            private Integer crc;
            /**
             * This is a version id used to allow backwards compatible evolution of the message binary format.
             * The current value is 0.
             */
            @Type(value = INT8, order = 1)
            private Byte magicByte;
            /**
             * This byte holds metadata attributes about the message. The lowest 2 bits contain the compression codec
             * used for the message. The other bits should be set to 0.
             */
            @Type(value = INT8, order = 2)
            private Byte attributes;
            /**
             * The key is an optional message key that was used for partition assignment. The key can be null.
             */
            @Type(value = BYTES, order = 3)
            private byte[] key;
            /**
             * The value is the actual message contents as an opaque byte array. Kafka supports recursive messages in
             * which case this may itself contain a message set. The message can be null.
             */
            @Type(value = BYTES, order = 4)
            private byte[] value;
        }
    }
}
