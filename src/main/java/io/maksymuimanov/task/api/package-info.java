/**
 * Contains API-related asynchronous components for the Concurrent API Aggregator Service.
 * <p>
 * This package defines interfaces and implementations responsible for making non-blocking
 * HTTP requests, fetching JSON data from external APIs, and aggregating results concurrently.
 * <p>
 * The {@link org.jspecify.annotations.NullUnmarked} annotation indicates that
 * nullability is unspecified by default within this package.
 *
 * @see io.maksymuimanov.task.api.AsyncApiRequestSender
 * @see io.maksymuimanov.task.api.AsyncApiFetcher
 * @see io.maksymuimanov.task.api.AsyncApiAggregator
 */
@NullUnmarked
package io.maksymuimanov.task.api;

import org.jspecify.annotations.NullUnmarked;