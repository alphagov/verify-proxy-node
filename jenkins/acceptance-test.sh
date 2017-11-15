#!/usr/bin/env bash

set -e

export PROXY_NODE_URL="https://verify-eidas-notification.cloudapps.digital"
./gradlew clean acceptanceTest
