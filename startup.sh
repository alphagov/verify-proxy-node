#!/usr/bin/env bash

set -euo pipefail

BUILD_DIR=".local_yaml"
PKI_DIR=".local_pki"
COMPONENTS="proxy-node-gateway proxy-node-translator stub-connector"
PN_PROJECT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
PKI_OUTPUT_DIR="${PN_PROJECT_DIR}/${PKI_DIR}"

(minikube status | grep -i running) || minikube start --memory 4096

mkdir -p "${BUILD_DIR}"
for component in $COMPONENTS; do
	tag="local-$(tar c $component | md5sum | awk '{print $1}')"
	image="govukverify/${component}:${tag}"
	echo "building $component as $image"
	if (eval $(minikube docker-env --shell bash) && docker inspect --type=image "${image}" >/dev/null 2>&1); then
		echo "already built"
	else
		docker build --file "${component}/Dockerfile" --build-arg "component=${component}" -t "${image}" .
		docker save "${image}" | (eval $(minikube docker-env --shell bash) && docker load)
	fi
	echo "generating kubeyaml from chart for ${image}"
	helm template "charts/${component}" \
		--name "${component}" \
		--output-dir "${BUILD_DIR}" \
		--set image.tag=${tag}
done

if [[ ! -e "${PKI_DIR}" ]]; then
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
fi

kubectl apply -R -f "${PKI_DIR}"
sleep 1
kubectl apply -R -f "${BUILD_DIR}"

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
