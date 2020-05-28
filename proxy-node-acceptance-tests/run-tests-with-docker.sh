#!/usr/bin/env sh
set -eu

echo "Before Docker compose build"
docker-compose build
echo "Docker compose build"
export PROXY_NODE_URL="http://localhost:6600/"
export STUB_CONNECTOR_URL="http://localhost:6610/"
export STUB_IDP_USER="stub-idp-demo-one"
docker-compose up -d selenium-hub

echo
docker-compose up --abort-on-container-exit acceptance-tests | grep acceptance-tests_1 --colour=never
#docker cp $(docker ps -a -q -f name="acceptance-tests"):/testreport .
docker-compose down
