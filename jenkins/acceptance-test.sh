#!/usr/bin/env bash

set -e

export PROXY_NODE_URL="https://verify-eidas-notification.cloudapps.digital"
export HUB_URL="http://stub-idp"
./gradlew clean acceptanceTest
