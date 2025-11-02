/**
 * Package-level annotation indicating that all types and members in this package
 * are assumed to be non-null by default, unless explicitly annotated otherwise.
 * This ensures stronger null-safety checks for all custom exceptions in the
 * asynchronous API aggregator service.
 *
 * @see io.maksymuimanov.task.exception.ApiAggregationException
 * @see io.maksymuimanov.task.exception.ApiFetchingException
 * @see io.maksymuimanov.task.exception.ApiRequestSendingException
 * @see io.maksymuimanov.task.exception.CacheManagingException
 * @see io.maksymuimanov.task.exception.HttpEndpointDirectingException
 * @see io.maksymuimanov.task.exception.HttpEndpointProcessionException
 * @see io.maksymuimanov.task.exception.HttpResponseSendingException
 * @see io.maksymuimanov.task.exception.HttpServerEndpointChannelInboundHandlingException
 * @see io.maksymuimanov.task.exception.HttpSocketChannelInitializingException
 * @see io.maksymuimanov.task.exception.NettyServerException
 */
@NullMarked
package io.maksymuimanov.task.exception;

import org.jspecify.annotations.NullMarked;