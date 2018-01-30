#!/usr/bin/env bash

set -e

# Start stub-idp locally

REMOTE="git@github.com:alphagov/ida-stub-idp"
APP_NAME="ida-stub-idp"
RUN_DIR="ida-stub-idp"
DIST_DIR="$RUN_DIR/build/distributions"
ZIP_DIR="$DIST_DIR/$APP_NAME"
CONFIG_FILE="$(pwd -P)/stub-idp/resources/local/configuration.yml"

test -d "$RUN_DIR" || git clone --quiet --depth 1 "$REMOTE" "$RUN_DIR" 

pushd "$RUN_DIR" >/dev/null
  git pull --quiet
  ./gradlew clean distZip
popd >/dev/null

pushd "$DIST_DIR" >/dev/null
  echo "Extracting distribution zip"
  unzip -q "${APP_NAME}.zip"
popd >/dev/null

echo "Running Stub IDP with Proxy Node config"

if [ "$DEBUG" = "true" ]; then
  echo "Running in Debug mode..."
  export JAVA_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
fi

(CONFIG_FILE=$CONFIG_FILE "$ZIP_DIR/bin/$APP_NAME" &) >/dev/null
