#!/usr/bin/env bash

set -euo pipefail

HELM_OUTPUT_DIR=".local_yaml"
PKI_DIR=".local_pki"
PN_PROJECT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
PKI_OUTPUT_DIR="${PN_PROJECT_DIR}/${PKI_DIR}"
MINIKUBE_IP="${PKI_OUTPUT_DIR}/minikube_ip"
TMP_COMPOSE=".components.yml"
TMP_HELM=".helm_values.yml"

mkdir -p "${HELM_OUTPUT_DIR}"

# Install required software using Homebrew
command -v yq >/dev/null || brew install yq
command -v jq >/dev/null || brew install jq

# Get components as defined by docker-compose.yml
yq read docker-compose.yml > $TMP_COMPOSE
components="$(yq read --tojson $TMP_COMPOSE | jq -r '.services | keys[]')"

for component in $components; do
  # Dereference symlinks when using tar so md5sum reflects Dockerfile changes
  tag="local-$(tar -ch $component | md5sum | awk '{print $1}')"
  image="govukverify/${component}:${tag}"
  echo "tagging $component: $image"
  yq write --inplace $TMP_COMPOSE "services.${component}.image" "$image"
done

echo "building images"
docker-compose -f $TMP_COMPOSE build --parallel

# Start minikube if not running
(minikube status | grep -i running) || minikube start --memory 4096 "${MINIKUBE_ARGS:-}"

# Push the locally-built images to minikube's docker
for component in $components; do
  image="$(yq read $TMP_COMPOSE "services.${component}.image")"
  docker tag "$image" "$(echo $image | sed 's/\(.\+\):.*/\1:latest/')"
  docker save "$image" | (eval $(minikube docker-env --shell bash) && docker load)
done

# Get the image definitions into a format suitable for Helm
yq read --tojson $TMP_COMPOSE services \
  | jq 'del(.[].build)' \
  | yq prefix - global \
  > $TMP_HELM

echo "generating kubeyaml from chart"
helm template "proxy-node-chart" \
  --name "local" \
  --output-dir "${HELM_OUTPUT_DIR}" \
  --values $TMP_HELM

rm -f $TMP_COMPOSE $TMP_HELM

function generate_pki {
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

    minikube ip > "$MINIKUBE_IP"
  popd
}

# Generate PKI if directory is missing
test -d "${PKI_OUTPUT_DIR}" || {
  echo "Generating PKI: No PKI directory"
  generate_pki
}

# Generate PKI if minikube IP has changed
test "$(minikube ip)" == "$(cat "$MINIKUBE_IP")" || {
  echo "Generating PKI: minikube IP has changed"
  generate_pki
}

kubectl apply -R -f "${PKI_OUTPUT_DIR}"
sleep 1
kubectl apply -R -f "${HELM_OUTPUT_DIR}"

function not_ready_count() {
  kubectl get po -o json | jq -r '.items[].status.conditions[].status' | grep False | wc -l | awk '{ print $1 }'
}

function not_running_count() {
  kubectl get po -o json | jq -r '.items[].status.phase' | grep -v Running | wc -l | awk '{ print $1 }'
}

sleep 2
while [[ "$(not_running_count)" != "0" ]]; do
  echo "waiting for $(not_running_count) pods to start"
  sleep 3
done
while [[ "$(not_ready_count)" != "0" ]]; do
  echo "waiting for $(not_ready_count) status probes to pass"
  sleep 3
done

echo ""
echo "http://$(minikube ip):31100/Request"
