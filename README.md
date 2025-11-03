# Concurrent API Aggregator Service

A non-blocking HTTP service built with `Netty` `(Java 21)` that exposes a single endpoint:

- GET `http://localhost:8080/api/dashboard` - concurrently fetches data from multiple public APIs and returns a unified JSON. If any upstream call fails, a cached value from Redis is returned.

## Installation & Build Instructions

### Prerequisites
- `Java 21` or newer installed
- `Redis` running locally or via `Docker`
- `Gradle`

### Starting via terminal
- For running the service locally with Redis as a `Docker` container:
```shell
./start-with-redis-on-docker.sh
```

- For running the service locally with already running Redis on `localhost:6379`:
```shell
./start.sh
```