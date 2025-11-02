/**
 * Provides asynchronous cache management components for the Concurrent API Aggregator Service.
 * <p>
 * This package contains interfaces and implementations for non-blocking cache access,
 * primarily backed by Redis. All operations are performed asynchronously using
 * {@link java.util.concurrent.CompletableFuture}, ensuring efficient handling in
 * reactive or Netty-based environments.
 * <p>
 * The {@link org.jspecify.annotations.NullMarked} annotation enforces non-null
 * behavior by default for all elements within this package.
 */
@NullMarked
package io.maksymuimanov.task.cache;

import org.jspecify.annotations.NullMarked;