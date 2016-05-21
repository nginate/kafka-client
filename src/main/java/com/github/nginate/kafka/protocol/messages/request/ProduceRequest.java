package com.github.nginate.kafka.protocol.messages.request;

import com.github.nginate.kafka.protocol.ApiKey;
import com.github.nginate.kafka.protocol.ApiKeys;
import com.github.nginate.kafka.protocol.messages.MessageSet;
import com.github.nginate.kafka.protocol.types.Type;
import lombok.Builder;
import lombok.Data;

import static com.github.nginate.kafka.protocol.types.TypeName.*;

/**
 * The produce API is used to send message sets to the server. For efficiency it allows sending message sets intended
 * for many topic partitions in a single request. The produce API uses the generic message set format, but since no
 * offset has been assigned to the messages at the time of the send the producer is free to fill in that field in any
 * way it likes.
 */
@Data
@Builder
@ApiKey(ApiKeys.PRODUCE)
public class ProduceRequest {
    /**
     * This field indicates how many acknowledgements the servers should receive before responding to the request.
     * If it is 0 the server will not send any response (this is the only case where the server will not reply to a
     * request). If it is 1, the server will wait the data is written to the local log before sending a response.
     * If it is -1 the server will block until the message is committed by all in sync replicas before sending
     * a response. For any number > 1 the server will block waiting for this number of acknowledgements to occur
     * (but the server will never wait for more acknowledgements than there are in-sync replicas).
     */
    @Type(value = INT16, order = 4)
    private Short requiredAcks;
    /**
     * This provides a maximum time in milliseconds the server can await the receipt of the number of acknowledgements
     * in RequiredAcks. The timeout is not an exact limit on the request time for a few reasons: (1) it does not include
     * network latency, (2) the timer begins at the beginning of the processing of this request so if many requests are
     * queued due to server overload that wait time will not be included, (3) we will not terminate a local write so if
     * the local write time exceeds this timeout it will not be respected. To get a hard timeout of this type the client
     * should use the socket timeout.
     */
    @Type(value = INT32, order = 5)
    private Integer timeout;

    @Type(value = WRAPPER, order = 6)
    private TopicProduceData[] topicProduceData;

    @Data
    @Builder
    public static class TopicProduceData {
        /**
         * The topic that data is being published to.
         */
        @Type(STRING)
        private String topic;

        @Type(value = WRAPPER, order = 1)
        private PartitionProduceData[] partitionProduceData;

        @Data
        @Builder
        public static class PartitionProduceData {
            /**
             * The partition that data is being published to.
             */
            @Type(INT32)
            private Integer partition;
            /**
             * The size, in bytes, of the message set that follows.
             */
            @Type(value = INT32, order = 1)
            private Integer messageSetSize;
            /**
             * A set of messages in the standard format described above.
             */
            @Type(value = WRAPPER, order = 2)
            private MessageSet messageSet;
        }

    }

}
