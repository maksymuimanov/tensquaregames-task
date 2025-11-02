/**
 * Contains Netty server components responsible for managing non-blocking HTTP connections,
 * channel pipelines, and event loop groups.
 * <p>
 * All classes in this package follow asynchronous and thread-safe design principles
 * using Netty’s event-driven architecture for scalable HTTP processing.
 *
 * @see io.maksymuimanov.task.server.HttpServerEndpointChannelInboundHandler
 * @see io.maksymuimanov.task.server.HttpSocketChannelInitializer
 * @see io.maksymuimanov.task.server.NettyServer
 */
@NullMarked
package io.maksymuimanov.task.server;

import org.jspecify.annotations.NullMarked;