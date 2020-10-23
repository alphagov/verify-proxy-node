#!/usr/bin/env bash

set -eu
temp_dir=$(mktemp -d)
cleanup () {
  rm -rf $temp_dir
}

trap cleanup EXIT

type kubectl >/dev/null 2>&1 || {
  echo >&2 "require kubectl but not installed.  Aborting."
  exit 1
}

type gds-cli >/dev/null 2>&1 || {
  echo >&2 "require gds-cli but not installed.  Aborting."
  exit 1
}

name="$1"
namespace="$2"
cert_pem="$3"
secret_key_pem="$4"

# taken from https://istio.io/v1.4/docs/tasks/traffic-management/ingress/secure-ingress-sds/#configure-a-tls-ingress-gateway-using-sds
kubectl create secret generic "$name" \
  --namespace "$namespace" \
  --from-file=cert="$cert_pem" \
  --from-file=key="$secret_key_pem" \
  --output yaml \
  --dry-run \
  >$temp_dir/unsealed.yaml

gds-cli verify seal --namespace "$namespace" --format yaml <$temp_dir/unsealed.yaml >"$name".yaml

echo "created sealed secret $name.yaml"

cleanup
