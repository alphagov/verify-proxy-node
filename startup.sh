#!/usr/bin/env bash

set -euo pipefail

PN_PROJECT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
PKI_OUTPUT_DIR="${PN_PROJECT_DIR}"/.local_pki

follow=false

while [ ! $# -eq 0 ]
do
  case "$1" in
    --follow)
      follow=true
      ;;
    *)
      echo "Usage $0 [--follow]"
      exit 1
      ;;
  esac
  shift
done

pushd "${PN_PROJECT_DIR}/pki"
  rm -f "${PKI_OUTPUT_DIR}/*"
  bundle install --quiet
  bundle exec generate \
    --hub-entity-id "https://dev-hub.local" \
    --idp-entity-id "http://stub_idp.acme.org/stub-idp-demo/SSO/POST" \
    --proxy-node-entity-id "_verify_proxy_node" \
    --hub-response-url "http://proxy-node-gateway/SAML2/SSO/Response/POST" \
    --idp-sso-url "http://stub-idp/stub-idp-demo/SAML2/SSO" \
    --proxy-sso-url "http://proxy-node-gateway/SAML2/SSO/POST" \
    --env \
    "${PKI_OUTPUT_DIR}"
popd

docker-compose up -d
if [ "$follow" = true ]
then
  docker-compose logs -f
fi
