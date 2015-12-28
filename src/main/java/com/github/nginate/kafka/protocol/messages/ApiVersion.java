package com.github.nginate.kafka.protocol.messages;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static java.util.Arrays.stream;

@RequiredArgsConstructor
public enum ApiVersion {
    V0(new Version(0, 8, 1)),
    V1(new Version(0, 8, 2)),
    V2(new Version(0, 8, 3)),
    UNDEFINED(Version.UNKNOWN_VERSION);

    @Getter
    private final Version since;

    public static ApiVersion from(Version version) {
        Optional<ApiVersion> first = stream(ApiVersion.values())
                .filter(apiVersion -> apiVersion.getSince().equals(version)).findFirst();
        return first.orElse(UNDEFINED);
    }

    public static ApiVersion from(String rawVersion) {
        return from(Version.parse(rawVersion));
    }
}
