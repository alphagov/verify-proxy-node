#!/usr/bin/env bash

set -e

function wait_for {
  local service="$1"
  local url="$2"
  local expected_code="${3:-200}"

  echo -n "Waiting for $service "
  until test "$expected_code" = $(curl --output /dev/null --silent --write-out '%{http_code}' "$url"); do
    echo -n "."
    sleep 1
  done
  echo " READY"
}

(./startup.sh --build --follow &) > ./logs/docker.log 2>&1

wait_for "CEF SP" localhost:56000
wait_for "CEF Connector" localhost:56001/ServiceProvider 500
wait_for "Stub IDP" localhost:56027/healthcheck
wait_for "Metadata" localhost:55000 403
wait_for "Gateway" localhost:56026/healthcheck
wait_for "Translator" localhost:56028/healthcheck

echo "Waiting 10 seconds for services."
sleep 10
./gradlew acceptanceTest
./shutdown.sh
