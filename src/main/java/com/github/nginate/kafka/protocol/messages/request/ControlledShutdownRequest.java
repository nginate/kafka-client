package com.github.nginate.kafka.protocol.messages.request;

import com.github.nginate.kafka.serialization.ApiKey;
import com.github.nginate.kafka.protocol.KafkaApiKeys;
import lombok.Builder;
import lombok.Data;

/**
 * In Kafka 0.8.0, was added a configurable controlled shutdown feature (controlled.shutdown.enable) that reduces
 * partition unavailability when brokers are bounced for upgrades or routine maintenance. The way this works is that if
 * a Kafka broker receives a request to shutdown and detects that controlled shutdown is enabled, it moves the leaders
 * from itself to other alive brokers in the cluster, before shutting down. If controlled shutdown is disabled,
 * the broker shuts down immediately and, from that time until the time the controller elects a new leader for all
 * partitions whose leaders lived on the dead broker, those partitions are unavailable. Depending on the size of the
 * cluster and the number of topic partitions, this time could be significant. For planned broker restarts, it is
 * desirable to move the leaders proactively so partitions are always writable even when individual brokers are
 * temporarily unavailable. Previously, this feature didn’t work well with automatic leader rebalancing and also with
 * single replica partitions, but those problems are fixed in 0.8.2, so controlled shutdown should be enabled by default
 * at all times on a production Kafka cluster.
 *
 * The Kafka cluster will automatically detect any broker shutdown or failure and elect new leaders for the partitions
 * on that machine. This will occur whether a server fails or it is brought down intentionally for maintenance or
 * configuration changes.
 * For the later cases Kafka supports a more graceful mechanism for stoping a server then just killing it. When a server
 * is stopped gracefully it has two optimizations it will take advantage of:
 *  It will sync all its logs to disk to avoid needing to do any log recovery when it restarts (i.e. validating the
 *  checksum for all messages in the tail of the log). Log recovery takes time so this speeds up intentional restarts.
 *
 *  It will migrate any partitions the server is the leader for to other replicas prior to shutting down. This will
 *  make the leadership transfer faster and minimize the time each partition is unavailable to a few milliseconds.
 *
 * Syncing the logs will happen automatically happen whenever the server is stopped other than by a hard kill, but the
 * controlled leadership migration requires using a special setting:
 *  controlled.shutdown.enable=true
 *
 * Note that controlled shutdown will only succeed if all the partitions hosted on the broker have replicas (i.e. the
 * replication factor is greater than 1 and at least one of these replicas is alive). This is generally what you want
 * since shutting down the last replica would make that topic partition unavailable.
 */
@Data
@Builder
@ApiKey(KafkaApiKeys.CONTROLLED_SHUTDOWN)
public class ControlledShutdownRequest {
}
