#!/usr/bin/env bash
set -e

if ! git diff -s --exit-code libs/*/pom.xml; then
  echo "Project dependencies have changed but updated pom files have not been checked in and committed"
  echo "Run './gradlew createPoms' and push changes to all files in the /libs directory to fix this"
  exit 1
fi
