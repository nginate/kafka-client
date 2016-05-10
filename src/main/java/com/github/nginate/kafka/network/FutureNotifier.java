/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.github.nginate.kafka.network;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import lombok.NonNull;

import java.util.concurrent.CompletableFuture;

/**
 * {@link GenericFutureListener} implementation which takes {@link CompletableFuture}s
 * and notifies it on completion.
 *
 * @param <F> the type of future
 */
public class FutureNotifier<F> implements FutureListener<F> {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(FutureNotifier.class);

    private final CompletableFuture<? super F> promise;

    /**
     * Create a new instance.
     *
     * @param promise the {@link CompletableFuture}s to notify once this {@link GenericFutureListener} is notified.
     */
    public FutureNotifier(@NonNull CompletableFuture<? super F> promise) {
        this.promise = promise;
    }

    @Override
    public void operationComplete(Future<F> future) throws Exception {
        if (future.isSuccess()) {
            if (!promise.complete(future.get())) {
                logger.warn("Failed to mark a promise as success because it is done already: {}", promise);
            }
        } else if (future.isCancelled()) {
            if (!promise.cancel(false)) {
                logger.warn("Failed to cancel a promise because it is done already: {}", promise);
            }
        } else {
            Throwable cause = future.cause();
            if (!promise.completeExceptionally(cause)) {
                logger.warn("Failed to mark a promise as failure because it's done already: {}", promise, cause);
            }
        }
    }
}
