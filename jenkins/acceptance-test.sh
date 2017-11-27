#!/usr/bin/env bash

set -e

export PROXY_NODE_URL="https://verify-eidas-notification.cloudapps.digital"
export HUB_URL="$PROXY_NODE_URL/stub-idp/request"

./gradlew clean acceptanceTest
