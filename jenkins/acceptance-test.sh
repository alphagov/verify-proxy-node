#!/usr/bin/env bash

set -e

export PROXY_NODE_URL="https://verify-eidas-notification.cloudapps.digital"

export IDP_URL="$PROXY_NODE_URL/stub-idp/request"
export CONNECTOR_NODE_URL="$PROXY_NODE_URL/connector-node/eidas-authn-request"
export PROXY_NODE_IDP_RESPONSE_URI="$PROXY_NODE_URL/SAML2/SSO/idp-response"
export PROXY_NODE_AUTHN_REQUEST_URI="$PROXY_NODE_URL/SAML2/SSO/POST"

./gradlew clean acceptanceTest
