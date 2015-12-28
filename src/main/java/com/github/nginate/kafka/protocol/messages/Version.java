package com.github.nginate.kafka.protocol.messages;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.io.Serializable;

import static java.util.Arrays.stream;

@Slf4j
@Value
public class Version implements Comparable<Version>, Serializable {

    public final static Version UNKNOWN_VERSION = new Version(0, 0, 0);
    private final int majorVersion;
    private final int minorVersion;
    private final int patchLevel;

    public Version(int major, int minor, int patch) {
        majorVersion = major;
        minorVersion = minor;
        patchLevel = patch;
    }

    public static Version parse(String value) {
        try {
            Integer[] values = stream(value.split(".")).map(Integer::parseInt).toArray(Integer[]::new);
            log.debug("Got values {} from incoming {}", values, value);
            int major = values.length > 0 ? values[0] : 0;
            int minor = values.length > 1 ? values[1] : 0;
            int patch = values.length > 2 ? values[2] : 0;
            return new Version(major, minor, patch);
        } catch (Exception e) {
            log.warn("Version parsing failed {}", e.getMessage(), e);
            return UNKNOWN_VERSION;
        }
    }

    @Override
    public String toString() {
        return String.valueOf(majorVersion) + '.' + minorVersion + '.' + patchLevel;
    }

    @Override
    public int compareTo(@Nonnull Version other) {
        if (other == this) return 0;

        int diff = majorVersion - other.majorVersion;
        if (diff == 0) {
            diff = minorVersion - other.minorVersion;
            if (diff == 0) {
                diff = patchLevel - other.patchLevel;
            }
        }
        return diff;
    }
}
