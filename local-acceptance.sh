#!/usr/bin/env bash

./startup.sh

until $(curl --output /dev/null --silent --head --header "Connection: keep-alive" http://localhost:56000/); do
  echo "Waiting for CEF"
  sleep 1
done

until $(curl --output /dev/null --silent --head --header "Connection: keep-alive" http://localhost:56016/); do
  echo "Waiting for Proxy Node"
  sleep 1
done

until $(curl --output /dev/null --silent --head --header "Connection: keep-alive" http://localhost:56017/); do
  echo "Waiting for Stub IDP"
  sleep 1
done

./gradlew acceptanceTest

./shutdown.sh
