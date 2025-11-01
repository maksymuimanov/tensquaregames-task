#!/bin/sh

wget https://download.redis.io/redis-stable.tar.gz

tar xzf redis-stable.tar.gz
cd redis-stable || exit

make

src/redis-server

cd ../

./gradlew clean build

./gradlew run