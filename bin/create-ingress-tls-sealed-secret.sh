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
cert_chain_pem_base64=$(base64 <"$3")
key_pem_base64=$(base64 <"$4")

kubectl create secret generic "$name" \
  --namespace "$namespace" \
  --from-literal=cert="$cert_chain_pem_base64" \
  --from-literal=key="$key_pem_base64" \
  --output yaml \
  --dry-run \
  >$temp_dir/unsealed.yaml

gds-cli verify seal --namespace "$namespace" --format yaml <$temp_dir/unsealed.yaml >"$name".yaml

echo "created sealed secret $name.yaml"

cleanup
