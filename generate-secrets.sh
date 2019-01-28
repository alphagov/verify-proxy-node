#!/usr/bin/env bash

set -euo pipefail

: "${DOMAIN:?}"
: "${RELEASE:?}"

PN_PROJECT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
PKI_DIR=".${DOMAIN}_${RELEASE}_pki"
PKI_OUTPUT_DIR="${PN_PROJECT_DIR}/${PKI_DIR}"

function generate_pki {
  pushd "${PN_PROJECT_DIR}/pki"
    rm -f "${PKI_OUTPUT_DIR}/*"
    bundle install --quiet
    bundle exec generate \
      --hub-entity-id "https://dev-hub.local" \
      --idp-entity-id "http://stub_idp.acme.org/stub-idp-demo/SSO/POST" \
      --proxy-node-entity-id "http://proxy-node" \
      --connector-url "https://${RELEASE}-stub-connector.${DOMAIN}" \
      --proxy-url "https://${RELEASE}-gateway.${DOMAIN}" \
      --idp-url "https://${RELEASE}-stub-idp.${DOMAIN}" \
      --softhsm \
      --secrets \
      "${PKI_OUTPUT_DIR}"
  popd
}

# Generate PKI if directory is missing
test -d "${PKI_OUTPUT_DIR}" || {
  echo "Generating PKI: No PKI directory"
  generate_pki
}
