package com.github.nginate.kafka.protocol;

import static com.github.nginate.kafka.util.StringUtils.format;
import static java.util.Arrays.stream;

/**
 * @see <a href=
 * "https://cwiki.apache.org/confluence/display/KAFKA/A+Guide+To+The+Kafka+Protocol#AGuideToTheKafkaProtocol
 * -Error">
 * Kafka protocol errors</a>
 */
public enum Error {
    /**
     * No error--it worked!
     */
    NO_ERROR(0),
    /**
     * An unexpected server error
     */
    UNKNOWN(-1),
    /**
     * The requested offset is outside the range of offsets maintained by the server for the given
     * topic/partition.
     */
    OFFSET_OUT_OF_RANGE(1),
    /**
     * This indicates that a message contents does not match its CRC
     */
    INVALID_MESSAGE(2),
    /**
     * This request is for a topic or partition that does not exist on this broker.
     */
    UNKNOWN_TOPIC_OR_PARTITION(3),
    /**
     * The message has a negative size
     */
    INVALID_MESSAGE_SIZE(4),
    /**
     * This error is thrown if we are in the middle of a leadership election and there is currently
     * no leader for this partition and hence it is unavailable for writes.
     */
    LEADER_NOT_AVAILABLE(5),
    /**
     * This error is thrown if the client attempts to send messages to a replica that is not the
     * leader for some partition. It indicates that the clients metadata is out of date.
     */
    NOT_LEADER_FOR_PARTITION(6),
    /**
     * This error is thrown if the request exceeds the user-specified time limit in the request.
     */
    REQUEST_TIMED_OUT(7),
    /**
     * This is not a client facing error and is used mostly by tools when a broker is not alive.
     */
    BROKER_NOT_AVAILABLE(8),
    /**
     * If replica is expected on a broker, but is not (this can be safely ignored).
     */
    REPLICA_NOT_AVAILABLE(9),
    /**
     * The server has a configurable maximum message size to avoid unbounded memory allocation. This
     * error is thrown if the client attempt to produce a message larger than this maximum.
     */
    MESSAGE_SIZE_TOO_LARGE(10),
    /**
     * Internal error code for broker-to-broker communication.
     */
    STALE_CONTROLLER_EPOCH(11),
    /**
     * If you specify a string larger than configured maximum for offset metadata
     */
    OFFSET_METADATA_TOO_LARGE(12),
    /**
     * The broker returns this error code for an offset fetch request if it is still loading offsets
     * (after a leader change for that offsets topic partition).
     */
    OFFSETS_LOAD_IN_PROGRESS(14),
    /**
     * The broker returns this error code for consumer metadata requests or offset commit requests
     * if the offsets topic has not yet been created.
     */
    CONSUMER_COORDINATOR_NOT_AVAILABLE(15),
    /**
     * The broker returns this error code if it receives an offset fetch or commit request for a
     * consumer group that it is not a coordinator for.
     */
    NOT_COORDINATOR_FOR_CONSUMER(16),
    /**
     * For a request which attempts to access an invalid topic (e.g. one which has an illegal name), or if an attempt is
     * made to write to an internal topic (such as the consumer offsets topic).
     */
    INVALID_TOPIC(17),
    /**
     * If a message batch in a produce request exceeds the maximum configured segment size.
     */
    RECORD_LIST_TOO_LARGE(18),
    /**
     * Returned from a produce request when the number of in-sync replicas is lower than the configured minimum and
     * requiredAcks is -1.
     */
    NOT_ENOUGH_REPLICAS(19),
    /**
     * Returned from a produce request when the message was written to the log, but with fewer in-sync replicas than
     * required.
     */
    NOT_ENOUGH_REPLICAS_AFTER_APPEND(20),
    /**
     * Returned from a produce request if the requested requiredAcks is invalid (anything other than -1, 1, or 0).
     */
    INVALID_REQUIRED_ACKS(21),
    /**
     * Returned from group membership requests (such as heartbeats) when the generation id provided in the request is
     * not the current generation.
     */
    ILLEGAL_GENERATION(22),
    /**
     * Returned in join group when the member provides a protocol type or set of protocols which is not compatible with
     * the current group.
     */
    INCONSISTENT_GROUP_PROTOCOL(23),
    /**
     * Returned in join group when the groupId is empty or null.
     */
    INVALID_GROUP_ID(24),
    /**
     * Returned from group requests (offset commits/fetches, heartbeats, etc) when the consumerId is not in the current
     * generation.
     */
    UNKNOWN_MEMBER_ID(25),
    /**
     * Return in join group when the requested session timeout is outside of the allowed range on the broker
     */
    INVALID_SESSION_TIMEOUT(26),
    /**
     * Returned in heartbeat requests when the coordinator has begun rebalancing the group. This indicates to the client
     * that it should rejoin the group.
     */
    REBALANCE_IN_PROGRESS(27),
    /**
     * This error indicates that an offset commit was rejected because of oversize metadata.
     */
    INVALID_COMMIT_OFFSET_SIZE(28),
    /**
     * Returned by the broker when the client is not authorized to access the requested topic.
     */
    TOPIC_AUTHORIZATION_FAILED(29),
    /**
     * Returned by the broker when the client is not authorized to access a particular groupId.
     */
    GROUP_AUTHORIZATION_FAILED(30),
    /**
     * Returned by the broker when the client is not authorized to use an inter-broker or administrative API.
     */
    CLUSTER_AUTHORIZATION_FAILED(31);

    private final int code;

    Error(int code) {
        this.code = code;
    }

    public static boolean isError(int code) {
        return code != NO_ERROR.code;
    }

    public static Error forCode(int code) {
        return stream(values()).filter(errorCodes -> errorCodes.code == code)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(format("Can't find error for code {}", code)));
    }
}
