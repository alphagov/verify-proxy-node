#!/usr/bin/env bash

set -e

export SERVICE_PROVIDER_URL="https://eidas-joint-14-sp.cloudapps.digital"
export CONNECTOR_NODE_URL="https://eidas-joint-14-connector.cloudapps.digital"
export PROXY_NODE_URL="https://verify-eidas-notification-proxy-node.cloudapps.digital"
export STUB_IDP_URL="https://verify-eidas-notification-stub-idp.cloudapps.digital"
export HUB_URL="$STUB_IDP_URL/stub-idp-demo/SAML2/SSO"
export CITIZEN_COUNTRY="UK_PN"

./gradlew clean acceptanceTest
