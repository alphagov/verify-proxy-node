#!/usr/bin/env bash
set -e

./docker-gradle clean -x test

./startup.sh

echo "Waiting 30 seconds for services."
sleep 30

echo ===========================================================
echo Running acceptance tests
echo ===========================================================

pushd notification-acceptance-tests > /dev/null
  $(pwd)/pre-commit.sh
popd > /dev/null
