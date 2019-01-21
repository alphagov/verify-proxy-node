#!/usr/bin/env bash
set -ueo pipefail

PN_PROJECT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

NODE_NAME=$(kubectl get nodes -o json | jq -r '.items[0].spec.externalID')

kubectl delete deployment --all

kubectl drain $NODE_NAME
sleep 5
kubectl uncordon $NODE_NAME
