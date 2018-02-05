#!/usr/bin/env bash

./shutdown.sh
(./startup.sh --build --follow &) > ./logs/docker.log

echo "Waiting for CEF"
until $(curl --output /dev/null --silent --head --header "Connection: keep-alive" http://localhost:56000/); do
  echo -n "."
  sleep 1
done
echo "CEF started"

echo "Waiting for Proxy Node"
until $(curl --output /dev/null --silent --head --header "Connection: keep-alive" http://localhost:56016/); do
  echo -n "."
  sleep 1
done
echo "Proxy Node started"

echo "Waiting for Stub IDP"
until $(curl --output /dev/null --silent --head --header "Connection: keep-alive" http://localhost:56017/); do
  echo -n "."
  sleep 1
done
echo "Stud IDP started"

./gradlew acceptanceTest

./shutdown.sh
