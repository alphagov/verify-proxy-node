#!/usr/bin/env bash

set -eu

. ./jenkins/login_to_paas.sh

# Update metadata
cp src/main/resources/paas/*.xml metadata/
pushd metadata/
  cf push
popd

./gradlew -x test pushToPaas
./jenkins/acceptance-test.sh
