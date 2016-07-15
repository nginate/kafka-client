package com.github.nginate.kafka.zookeeper;

import com.github.nginate.kafka.zookeeper.exception.ZookeeperValidationException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.github.nginate.kafka.zookeeper.TopicConfigEntry.CleanupPolicy.DELETE;
import static com.github.nginate.kafka.zookeeper.TopicConfigEntry.CompressionType.PRODUCER;
import static com.github.nginate.kafka.zookeeper.TopicConfigEntry.MessageTimestampType.CREATE;
import static java.util.Arrays.stream;

@RequiredArgsConstructor
public enum TopicConfigEntry {
    SEGMENT_BYTES("segment.bytes", 1024 * 1024 * 1024, NumberUtils::createInteger,
            o -> Integer.class.isAssignableFrom(o.getClass()) && Integer.class.cast(o) >= 14),
    SEGMENT_MS("segment.ms", 24 * 7 * 60 * 60 * 1000L, NumberUtils::createLong,
            o -> Long.class.isAssignableFrom(o.getClass()) && Long.class.cast(o) >= 0),
    SEGMENT_JITTER_MS("segment.jitter.ms", 0, NumberUtils::createLong,
            o -> Long.class.isAssignableFrom(o.getClass()) && Long.class.cast(o) >= 0),
    SEGMENT_INDEX_BYTES("segment.index.bytes", 10 * 1024 * 1024, NumberUtils::createInteger,
            o -> Integer.class.isAssignableFrom(o.getClass()) && Integer.class.cast(o) >= 0),
    FLUSH_MESSAGES("flush.messages", Long.MAX_VALUE, NumberUtils::createLong,
            o -> Long.class.isAssignableFrom(o.getClass()) && Long.class.cast(o) >= 0),
    FLUSH_MS("flush.ms", Long.MAX_VALUE, NumberUtils::createLong,
            o -> Long.class.isAssignableFrom(o.getClass()) && Long.class.cast(o) >= 0),
    RETENTION_BYTES("retention.bytes", -1L, NumberUtils::createLong,
            o -> Long.class.isAssignableFrom(o.getClass())),
    RETENTION_MS("retention.ms", 24 * 7 * 60 * 60 * 1000L, NumberUtils::createLong,
            o -> Long.class.isAssignableFrom(o.getClass())),
    MAX_MESSAGE_BYTES("max.message.bytes", 1000000 + /*LogOverhead*/ 4 /*MessageSizeLength*/ * 8 /*OffsetLength*/,
            NumberUtils::createInteger,
            o -> Integer.class.isAssignableFrom(o.getClass()) && Integer.class.cast(o) >= 0),
    INDEX_INTERVAL_BYTES("index.interval.bytes", 4096, NumberUtils::createInteger,
            o -> Integer.class.isAssignableFrom(o.getClass()) && Integer.class.cast(o) >= 0),
    DELETE_RETENTION_MS("delete.retention.ms", 24 * 60 * 60 * 1000L, NumberUtils::createLong,
            o -> Long.class.isAssignableFrom(o.getClass()) && Long.class.cast(o) >= 0),
    FILE_DELETE_DELAY_MS("file.delete.delay.ms", 60000L, NumberUtils::createLong,
            o -> Long.class.isAssignableFrom(o.getClass()) && Long.class.cast(o) >= 0),
    MIN_CLEANABLE_DIRTY_RATIO("min.cleanable.dirty.ratio", 0.5d, NumberUtils::createDouble,
            o -> Double.class.isAssignableFrom(o.getClass()) && Range.between(0d, 1d).contains(Double.class.cast(o))),
    CLEANUP_POLICY("cleanup.policy", DELETE, s -> EnumUtils.getEnum(CleanupPolicy.class, s.toUpperCase()),
            o -> EnumUtils.isValidEnum(CleanupPolicy.class, o.toString())),
    UNCLEAN_LEADER_ELECTION_ENABLE("unclean.leader.election.enable", true, BooleanUtils::toBoolean,
            o -> Boolean.class.isAssignableFrom(o.getClass())),
    MIN_IN_SYNC_REPLICA("min.insync.replicas", 1, NumberUtils::createInteger,
            o -> Integer.class.isAssignableFrom(o.getClass()) && Integer.class.cast(o) >= 1),
    COMPRESSION_TYPE("compression.type", PRODUCER, s -> EnumUtils.getEnum(CompressionType.class, s.toUpperCase()),
            o -> EnumUtils.isValidEnum(CompressionType.class, o.toString())),
    PRE_ALLOCATE_ENABLE("preallocate", false, BooleanUtils::toBoolean,
            o -> Boolean.class.isAssignableFrom(o.getClass())),
    /**
     * @see com.github.nginate.kafka.serialization.ApiVersion
     */
    MESSAGE_FORMAT_VERSION("message.format.version", 2, NumberUtils::createInteger,
            o -> Integer.class.isAssignableFrom(o.getClass())),
    MESSAGE_TIMESTAMP_TYPE("message.timestamp.type", CREATE, s -> EnumUtils.getEnum(MessageTimestampType.class, s),
            o -> MessageTimestampType.fromValue(o.toString()).isPresent()),
    MESSAGE_TIMESTAMP_DIFFERENCE_MAX_MS("message.timestamp.difference.max.ms", Long.MAX_VALUE, NumberUtils::createLong,
            o -> Long.class.isAssignableFrom(o.getClass()) && Long.class.cast(o) >= 0);

    private static final Map<String, TopicConfigEntry> configEntries = new HashMap<>();

    static {
        stream(TopicConfigEntry.values()).forEach(entry -> configEntries.put(entry.getName(), entry));
    }

    @Getter
    private final String name;
    @Getter
    private final Object defaultValue;
    private final Function<String, Object> parseFunction;
    private final Function<Object, Boolean> validationFunction;

    public static boolean isValidEntry(String name, String value) {
        TopicConfigEntry topicConfigEntry = configEntries.get(name);
        if (topicConfigEntry == null) {
            throw new ZookeeperValidationException("Unknown topic config entry name : " + name);
        }
        return topicConfigEntry.validationFunction.apply(value);
    }

    @RequiredArgsConstructor
    public enum CleanupPolicy {
        DELETE("delete"), COMPACT("compact");
        @Getter
        private final String value;
    }

    @RequiredArgsConstructor
    public enum CompressionType {
        PRODUCER("producer"), UNCOMPRESSED("uncompressed"), SNAPPY("snappy"), LZ4("lz4"), GZIP("gzip");
        @Getter
        private final String value;

    }

    @RequiredArgsConstructor
    public enum MessageTimestampType {
        CREATE("CreateTime");
        @Getter
        private final String value;

        public static Optional<MessageTimestampType> fromValue(String value) {
            return stream(MessageTimestampType.values())
                    .filter(type -> type.getValue().equalsIgnoreCase(value))
                    .findAny();
        }
    }
}
