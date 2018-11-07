#!/usr/bin/env bash

set -euo pipefail

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
