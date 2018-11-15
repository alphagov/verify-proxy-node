#!/usr/bin/env bash
set -e

./docker-gradle clean test

./startup.sh

echo "Waiting 60 seconds for services."
sleep 60

echo ===========================================================
echo Running acceptance tests
echo ===========================================================

pushd proxy-node-acceptance-tests > /dev/null
  $(pwd)/pre-commit.sh
popd > /dev/null
