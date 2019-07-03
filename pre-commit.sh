#!/usr/bin/env bash
set -e

./gradlew clean test

if ! git diff -s --exit-code libs/*/pom.xml; then
  echo '⚠️ Remember to commit and push updated project dependencies files (pom.xml) ⚠️'
  git add libs/*/pom.xml
fi

