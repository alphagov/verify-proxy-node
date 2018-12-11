#!/usr/bin/env bash

set -euo pipefail

PN_PROJECT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
PKI_OUTPUT_DIR="${PN_PROJECT_DIR}"/.local_pki

minikube start

pushd "${PN_PROJECT_DIR}/pki"
  rm -f "${PKI_OUTPUT_DIR}/*"
  bundle install --quiet
  bundle exec generate \
    --hub-entity-id "https://dev-hub.local" \
    --idp-entity-id "http://stub_idp.acme.org/stub-idp-demo/SSO/POST" \
    --proxy-node-entity-id "http://proxy-node" \
    --connector-url "http://$(minikube ip):31100" \
    --proxy-url "http://$(minikube ip):31200" \
    --idp-url "http://$(minikube ip):31300" \
    --softhsm \
    --configmaps \
    "${PKI_OUTPUT_DIR}"
popd

kubectl delete -R -f yaml/ || :
kubectl apply -R -f .local_pki/
kubectl apply -R -f yaml/

echo ""
echo "http://$(minikube ip):31100/Request"
