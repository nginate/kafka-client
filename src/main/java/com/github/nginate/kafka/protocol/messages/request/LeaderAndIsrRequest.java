package com.github.nginate.kafka.protocol.messages.request;

import com.github.nginate.kafka.protocol.ApiKey;
import com.github.nginate.kafka.protocol.ApiKeys;
import com.github.nginate.kafka.protocol.messages.dto.Broker;
import com.github.nginate.kafka.protocol.types.Type;
import lombok.Builder;
import lombok.Data;

import static com.github.nginate.kafka.protocol.types.TypeName.INT32;
import static com.github.nginate.kafka.protocol.types.TypeName.STRING;
import static com.github.nginate.kafka.protocol.types.TypeName.WRAPPER;

@Data
@Builder
@ApiKey(ApiKeys.LEADER_AND_ISR)
public class LeaderAndIsrRequest {
    @Type(value = INT32, order = 4)
    private Integer controllerId;
    @Type(value = INT32, order = 5)
    private Integer controllerEpoch;
    @Type(value = WRAPPER, order = 6)
    private PartitionStateInfoWrapper[] partitionStateInfoWrappers;
    @Type(value = WRAPPER, order = 7)
    private Broker[] leaders;

    @Data
    @Builder
    public static class PartitionStateInfoWrapper {
        @Type(STRING)
        private String topic;
        @Type(value = INT32, order = 1)
        private Integer partition;
        @Type(value = WRAPPER, order = 2)
        private PartitionStateInfo[] partitionStateInfos;

        @Data
        @Builder
        public static class PartitionStateInfo {
            @Type(WRAPPER)
            private LeaderIsrAndControllerEpoch leaderIsrAndControllerEpoch;
            @Type(value = INT32, order = 1)
            private Integer[] allReplicas;

            @Data
            public static class LeaderIsrAndControllerEpoch {
                @Type(WRAPPER)
                private LeaderAndIsr leaderAndIsr;
                @Type(value = INT32, order = 1)
                private Integer controllerEpoch;

                @Data
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
    }
}
