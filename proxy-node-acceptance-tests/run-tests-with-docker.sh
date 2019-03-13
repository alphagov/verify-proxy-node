#!/usr/bin/env sh
set -eu

echo "Before Docker compose build"
docker-compose build
echo "Docker compose build"
export PROXY_NODE_URL="http://$(minikube ip):31200"
export STUB_CONNECTOR_URL="http://$(minikube ip):31100"
export STUB_IDP_USER=""
docker-compose up --abort-on-container-exit | grep acceptance-tests_1 --colour=never
docker cp $(docker ps -a -q -f name="acceptance-tests"):/testreport .
docker-compose down
