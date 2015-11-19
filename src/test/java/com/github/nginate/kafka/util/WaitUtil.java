package com.github.nginate.kafka.util;

import com.github.nginate.kafka.exceptions.TimeoutException;
import com.google.common.base.Preconditions;
import lombok.experimental.UtilityClass;

import java.util.function.Supplier;

import static java.lang.System.currentTimeMillis;

@UtilityClass
public final class WaitUtil {

    public static void waitUntil(long waitMillis, long waitStepMillis, Supplier<Boolean> condition) {
        waitUntil(waitMillis, waitStepMillis, condition, "Could not get required condition in timeout : " + waitMillis);
    }

    public static void waitUntil(long waitMillis, long waitStepMillis, Supplier<Boolean> condition, String failureMessage) {
        Preconditions.checkArgument(waitStepMillis >= waitMillis, "step sleep time must be less or equal to timeout");
        long start = currentTimeMillis();
        try {
            while (!condition.get()) {
                Thread.sleep(waitMillis);
                if (currentTimeMillis() - start >= waitMillis) {
                    throw new TimeoutException(failureMessage);
                }
            }
        } catch (InterruptedException e) {
            throw new TimeoutException(e.getMessage(), e);
        }
    }
}
