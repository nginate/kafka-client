package com.github.nginate.kafka.protocol.messages;

import lombok.Getter;

import java.util.Optional;

import static java.util.Arrays.stream;

public enum ApiVersion {
    V0(new Version(0, 8, 1), (short) 0),
    V1(new Version(0, 8, 2), (short) 1),
    V2(new Version(0, 8, 3), (short) 2),
    UNDEFINED(Version.UNKNOWN_VERSION, (short) -1);

    @Getter
    private final Version since;
    @Getter
    private final Short versionKey;

    ApiVersion(Version since, Short versionKey) {
        this.since = since;
        this.versionKey = versionKey;
    }

    public static ApiVersion from(Version version) {
        Optional<ApiVersion> first = stream(ApiVersion.values())
                .filter(apiVersion -> apiVersion.getSince().equals(version)).findFirst();
        return first.orElse(UNDEFINED);
    }

    public static ApiVersion from(String rawVersion) {
        return from(Version.parse(rawVersion));
    }
}
