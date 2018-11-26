#!/usr/bin/env bash

set -euo pipefail

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

PN_PROJECT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
PKI_OUTPUT_DIR="${PN_PROJECT_DIR}"/.local_pki

pushd "${PN_PROJECT_DIR}/pki"
  rm -f "${PKI_OUTPUT_DIR}/*"
  bundle install --quiet
  bundle exec generate \
    --hub-entity-id "https://dev-hub.local" \
    --idp-entity-id "http://stub_idp.acme.org/stub-idp-demo/SSO/POST" \
    --proxy-node-entity-id "http://proxy-node" \
    --hub-response-url "http://localhost:6100/SAML2/SSO/Response/POST" \
    --idp-sso-url "http://localhost:6200/stub-idp-demo/SAML2/SSO" \
    --proxy-sso-url "http://localhost:6100/SAML2/SSO/POST" \
    --env \
    "${PKI_OUTPUT_DIR}"
popd

docker-compose up $@

wait_for "Gateway"  localhost:6601/healthcheck
wait_for "Translator" localhost:6661/healthcheck
wait_for "Stub Connector" localhost:6667/healthcheck

