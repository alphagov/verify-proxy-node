#!/usr/bin/env bash

set -e

./startup.sh
./gradlew acceptanceTest
./kill-service.sh

