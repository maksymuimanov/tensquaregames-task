# Concurrent API Aggregator Service

[GitHub Repository](https://github.com/maksymuimanov/tensquaregames-task)

A non-blocking HTTP service built with `Netty` that exposes a single endpoint:

- GET `http://localhost:8080/api/dashboard` - concurrently fetches data from multiple public APIs and returns a unified JSON. If any upstream call fails, a cached value from Redis is returned.

## Installation & Build Instructions

### Prerequisites
- `Java 21` or newer installed
- `Redis` running locally (`localhost:6379`)
- `Docker` if there is no Redis running locally, or the Redis instance is running on a different host (also mandatory for running tests)
- `Gradle`

### Starting via terminal
Firstly you have to extract the .tar.gz file. Then, run
```shell
cd PATH_TO_EXTRACTED_PROJECT
```
into the project directory.

- For running the service locally with Redis as a `Docker` container:
```shell
./start-with-redis-on-docker.sh
```

- For running the service locally with already running Redis on `localhost:6379`:
```shell
./start.sh
```