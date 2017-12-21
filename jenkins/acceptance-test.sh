#!/usr/bin/env bash

set -e

export PROXY_NODE_URL="https://verify-eidas-notification.cloudapps.digital"
export STUB_IDP_URL="https://verify-eidas-notification-stub-idp.cloudapps.digital"
export HUB_URL="$STUB_IDP_URL/stub-idp-demo/SAML2/SSO"

./gradlew clean acceptanceTest
