#!/usr/bin/env bash

set -euo pipefail

BUILD_DIR=".local_yaml"
PKI_DIR=".local_pki"
COMPONENTS="proxy-node-gateway proxy-node-translator stub-connector"
PN_PROJECT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
PKI_OUTPUT_DIR="${PN_PROJECT_DIR}/${PKI_DIR}"

# boot a local k8s cluster
(minikube status | grep -i running) || minikube start --memory 4096

# clean
# if [[ -e "${BUILD_DIR}" ]]; then
# 	kubectl delete -R -f "${BUILD_DIR}" || echo 'fine, continue'
# 	rm -rf "${BUILD_DIR}"
# fi
# if [[ -e "${PKI_DIR}" ]]; then
# 	kubectl delete -R -f "${PKI_DIR}" || echo 'fine, continue'
# 	rm -rf "${PKI_DIR}"
# fi

# build all the images locally
# send all the images to the dockerdaemon in minikube
mkdir -p "${BUILD_DIR}"
for component in $COMPONENTS; do
	tag="local-$(tar c $component | md5sum | awk '{print $1}')"
	image="govukverify/${component}:${tag}"
	echo "building $component as $image"
	if (eval $(minikube docker-env --shell bash) && docker inspect --type=image "${image}" >/dev/null 2>&1); then
		echo "already built"
	else
		docker build --build-arg "component=${component}" -t "${image}" .
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

# generate yaml from helm charts

kubectl apply -R -f "${PKI_DIR}"
sleep 1
kubectl apply -R -f "${BUILD_DIR}"

echo ""
echo "http://$(minikube ip):31100/Request"
