#!/usr/bin/env bash

set -euo pipefail

: "${DOMAIN:?}"
: "${RELEASE:?}"
: "${HUB_ENTITY_ID:?}" # ie https://dev-hub.local
: "${IDP_ENTITY_ID:?}" # ie http://stub_idp.acme.org/stub-idp-demo/SSO/POST

PN_PROJECT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
PKI_DIR=".${DOMAIN}_${RELEASE}_pki"
PKI_OUTPUT_DIR="${PN_PROJECT_DIR}/${PKI_DIR}"

function generate_pki {
  pushd "${PN_PROJECT_DIR}/pki"
    rm -f "${PKI_OUTPUT_DIR}/*"
    bundle install --quiet
    bundle exec generate \
      --hub-entity-id "${HUB_ENTITY_ID}" \
      --idp-entity-id "${IDP_ENTITY_ID}" \
      --proxy-node-entity-id "https://${RELEASE}-proxy-node.${DOMAIN}" \
      --connector-url "https://${RELEASE}-connector.${DOMAIN}" \
      --proxy-url "https://${RELEASE}-gateway.${DOMAIN}" \
      --idp-url "https://${RELEASE}-stub-idp.${DOMAIN}" \
      --secrets \
      "${PKI_OUTPUT_DIR}"
  popd
}

# Generate PKI if directory is missing
test -d "${PKI_OUTPUT_DIR}" || {
  echo "Generating PKI: No PKI directory"
  generate_pki
}
