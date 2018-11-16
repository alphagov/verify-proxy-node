#!/usr/bin/env bash
set -e

./docker-gradle clean test

./startup.sh


echo ===========================================================
echo Running acceptance tests
echo ===========================================================

pushd proxy-node-acceptance-tests > /dev/null
  $(pwd)/pre-commit.sh
popd > /dev/null
