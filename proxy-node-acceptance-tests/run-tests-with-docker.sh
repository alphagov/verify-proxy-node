#!/usr/bin/env sh
set -u

echo "Before Docker compose build"
docker-compose build
echo "Docker compose build"
export STUB_CONNECTOR_URL="http://$(minikube ip):31100/Request"
docker-compose up --abort-on-container-exit
docker cp $(docker ps -a -q -f name="acceptance-tests"):/testreport .
docker-compose down
