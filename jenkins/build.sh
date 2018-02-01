#!/usr/bin/env bash

set -e

./gradlew clean build
./local-acceptance.sh
./gradlew publish