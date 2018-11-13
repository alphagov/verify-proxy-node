#!/usr/bin/env bash

set -e

docker-compose -f test-compose.yaml build proxy-node-tests
docker-compose -f test-compose.yaml run --rm proxy-node-tests
