package com.github.nginate.kafka.protocol.messages.dto;

import com.github.nginate.kafka.protocol.types.Type;
import lombok.Builder;
import lombok.Data;

import static com.github.nginate.kafka.protocol.types.TypeName.INT32;
import static com.github.nginate.kafka.protocol.types.TypeName.WRAPPER;

@Data
@Builder
public class PartitionStateInfo {
    @Type(WRAPPER)
    private LeaderIsrAndControllerEpoch leaderIsrAndControllerEpoch;
    @Type(value = INT32, order = 1)
    private Integer[] allReplicas;

    @Data
    @Builder
    public static class LeaderIsrAndControllerEpoch {
        @Type(WRAPPER)
        private LeaderAndIsr leaderAndIsr;
        @Type(value = INT32, order = 1)
        private Integer controllerEpoch;

        @Data
        @Builder
        public static class LeaderAndIsr {
            @Type(INT32)
            private Integer leader;
            @Type(value = INT32, order = 1)
            private Integer leaderEpoch;
            @Type(value = INT32, order = 2)
            private Integer[] isr;
            @Type(value = INT32, order = 3)
            private Integer zkVersion;
        }
    }
}
