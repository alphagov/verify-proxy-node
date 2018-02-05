#!/usr/bin/env bash

function wait_for {
  local service="$1"
  local port="$2"

  echo -n "Waiting for $service "
  until $(curl --output /dev/null --silent --head --header "Connection: keep-alive" "http://localhost:$port/"); do
    echo -n "."
    sleep 1
  done
  echo " READY"
}

./shutdown.sh
(./startup.sh --build --follow &) > ./logs/docker.log

wait_for "CEF" 56000
wait_for "Proxy Node" 56016
wait_for "Stub IDP" 56017

./gradlew acceptanceTest
./shutdown.sh
