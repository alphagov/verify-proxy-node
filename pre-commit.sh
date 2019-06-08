#!/usr/bin/env bash
set -e

./gradlew clean test

if ! git diff -s --exit-code */pom.xml; then
  echo '⚠️ Remember to commit and push updated project dependencies file (pom.xml) ⚠️'
  git add */pom.xml
fi

./startup.sh --build


echo ===========================================================
echo Running acceptance tests
echo ===========================================================

export STUB_CONNECTOR_URL="http://$(minikube ip):31100/Request"

pushd proxy-node-acceptance-tests > /dev/null
  $(pwd)/run-tests-with-docker.sh
popd > /dev/null
