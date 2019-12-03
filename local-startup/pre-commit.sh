#!/usr/bin/env bash
set -e

../gradlew clean test

./startup.sh --build


echo ===========================================================
echo Running acceptance tests
echo ===========================================================

export STUB_CONNECTOR_URL="http://$(minikube ip):31100/Request"

pushd proxy-node-acceptance-tests > /dev/null
  $(pwd)/run-tests-with-docker.sh
popd > /dev/null
