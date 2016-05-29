package com.github.nginate.kafka.protocol.messages.request;

import com.github.nginate.kafka.protocol.KafkaApiKeys;
import com.github.nginate.kafka.protocol.messages.dto.Broker;
import com.github.nginate.kafka.serialization.ApiKey;
import com.github.nginate.kafka.serialization.ApiVersion;
import com.github.nginate.kafka.serialization.Type;
import lombok.Builder;
import lombok.Data;

import static com.github.nginate.kafka.serialization.TypeName.*;

@Data
@Builder
@ApiKey(KafkaApiKeys.LEADER_AND_ISR)
@ApiVersion(0)
public class LeaderAndIsrRequest {
    @Type(value = INT32, order = 4)
    private Integer controllerId;
    @Type(value = INT32, order = 5)
    private Integer controllerEpoch;
    @Type(value = WRAPPER, order = 6)
    private LeaderAndIsrRequestPartitionState[] partitionStates;
    @Type(value = WRAPPER, order = 7)
    private Broker[] liveLeaders;

    @Data
    public static class LeaderAndIsrRequestPartitionState {
        /**
         * Topic name.
         */
        @Type(STRING)
        private String topic;

        /**
         * Topic partition id.
         */
        @Type(value = INT32, order = 1)
        private Integer partition;

        /**
         * The controller epoch.
         */
        @Type(value = INT32, order = 2)
        private Integer controllerEpoch;

        /**
         * The broker id for the leader.
         */
        @Type(value = INT32, order = 3)
        private Integer leader;

        /**
         * The leader epoch.
         */
        @Type(value = INT32, order = 4)
        private Integer leaderEpoch;

        /**
         * The in sync replica ids.
         */
        @Type(value = INT32, order = 5)
        private Integer[] isr;

        /**
         * The ZK version.
         */
        @Type(value = INT32, order = 6)
        private Integer[] zkVersion;

        /**
         * The replica ids.
         */
        @Type(value = INT32, order = 7)
        private Integer[] replicas;
    }
}
