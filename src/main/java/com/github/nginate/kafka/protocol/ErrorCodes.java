package com.github.nginate.kafka.protocol;

import lombok.experimental.UtilityClass;

/**
 * @see <a href="https://cwiki.apache.org/confluence/display/KAFKA/A+Guide+To+The+Kafka+Protocol#AGuideToTheKafkaProtocol-ErrorCodes">Kafka protocol errors</a>
 */
@UtilityClass
public final class ErrorCodes {
    /**
     * No error--it worked!
     */
    public static final int NoError = 0;
    /**
     * An unexpected server error
     */
    public static final int Unknown = -1;
    /**
     * The requested offset is outside the range of offsets maintained by the server for the given topic/partition.
     */
    public static final int OffsetOutOfRange = 1;
    /**
     * This indicates that a message contents does not match its CRC
     */
    public static final int InvalidMessage = 2;
    /**
     * This request is for a topic or partition that does not exist on this broker.
     */
    public static final int UnknownTopicOrPartition = 3;
    /**
     * The message has a negative size
     */
    public static final int InvalidMessageSize = 4;
    /**
     * This error is thrown if we are in the middle of a leadership election and there is currently no leader for this partition and hence it is unavailable for writes.
     */
    public static final int LeaderNotAvailable = 5;
    /**
     * This error is thrown if the client attempts to send messages to a replica that is not the leader for some partition. It indicates that the clients metadata is out of date.
     */
    public static final int NotLeaderForPartition = 6;
    /**
     * This error is thrown if the request exceeds the user-specified time limit in the request.
     */
    public static final int RequestTimedOut = 7;
    /**
     * This is not a client facing error and is used mostly by tools when a broker is not alive.
     */
    public static final int BrokerNotAvailable = 8;
    /**
     * If replica is expected on a broker, but is not (this can be safely ignored).
     */
    public static final int ReplicaNotAvailable = 9;
    /**
     * The server has a configurable maximum message size to avoid unbounded memory allocation. This error is thrown if the client attempt to produce a message larger than this maximum.
     */
    public static final int MessageSizeTooLarge = 10;
    /**
     * Internal error code for broker-to-broker communication.
     */
    public static final int StaleControllerEpochCode = 11;
    /**
     * If you specify a string larger than configured maximum for offset metadata
     */
    public static final int OffsetMetadataTooLargeCode = 12;
    /**
     * The broker returns this error code for an offset fetch request if it is still loading offsets (after a leader change for that offsets topic partition).
     */
    public static final int OffsetsLoadInProgressCode = 14;
    /**
     * The broker returns this error code for consumer metadata requests or offset commit requests if the offsets topic has not yet been created.
     */
    public static final int ConsumerCoordinatorNotAvailableCode = 15;
    /**
     * The broker returns this error code if it receives an offset fetch or commit request for a consumer group that it is not a coordinator for.
     */
    public static final int NotCoordinatorForConsumerCode = 16;
}
