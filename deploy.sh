#!/bin/bash

set -eu

: "${RELEASE:?}"
: "${ENVIRONMENT:?}"

export DOMAIN="${ENVIRONMENT}.verify.govsvc.uk"
export KUBECONFIG="$HOME/src/gsp/gsp-teams/terraform/accounts/verify/clusters/${ENVIRONMENT}/kubeconfig"

# apply the proxy-node yaml
helm template proxy-node-chart/ \
	--namespace "${RELEASE}-proxy-node" \
	--name "${RELEASE}" \
	--set "global.cluster.domain=${DOMAIN}" \
	--set "global.cluster.name=${ENVIRONMENT}" \
	--set "hsm.IP=$(aws ssm get-parameter --query Parameter.Value --output text --with-decryption --name /hsm/ip)" \
	--set "hsm.user=cu1" \
	--set "hsm.base64EncodedPassword=$(aws ssm get-parameter --query Parameter.Value --output text --with-decryption --name /hsm/users/cu/1/password | tr -d '\n' | base64)" \
	--set "hsm.customerCA=$(aws ssm get-parameter --query Parameter.Value --output text --with-decryption --name /hsm/customerCA)" \
	--set "hsm.image.tag=v0.0.0-dev-e48b1eb" \
	--set "esp.image.tag=v0.0.0-dev-e48b1eb" \
	--set "gateway.image.tag=v0.0.0-dev-e48b1eb" \
	--set "translator.image.tag=v0.0.0-dev-e48b1eb" \
	--set "stubConnector.enabled=true" \
	--set "stubConnector.image.tag=v0.0.0-dev-e48b1eb" \
	--set "vmc.image.tag=v0.0.0-dev-20c5227" \
	--set "vsp.image.tag=v0.0.0-dev-2503371" \
	| kubectl -n "${RELEASE}-proxy-node" apply -f -

# TODO: seal vsp secrets... 
kubectl -n "${RELEASE}-proxy-node" apply -f "${RELEASE}-vsp-secret.yaml"

# TODO: seal hsm secrets...

