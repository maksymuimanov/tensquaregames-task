#!/bin/sh

docker run -d --rm --name redis-test-task-container -p 6379:6379 redis:7-alpine

./start.sh

docker stop redis-test-task-container