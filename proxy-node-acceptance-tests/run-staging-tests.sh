#!/usr/bin/env sh
set -eu

echo "Before Docker compose build"
docker-compose build
echo "Docker compose build"
export PROXY_NODE_URL="https://proxy-node.eidas.test.london.verify.govsvc.uk"
export STUB_CONNECTOR_URL="https://stub-connector.eidas.test.london.verify.govsvc.uk"
export STUB_IDP_USER="stub-idp-demo-one"
docker-compose up --abort-on-container-exit | grep acceptance-tests_1 --colour=never
docker cp $(docker ps -a -q -f name="acceptance-tests"):/testreport .
docker-compose down
