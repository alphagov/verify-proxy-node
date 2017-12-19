#!/usr/bin/env bash

# Start stub-idp locally

APP_NAME="ida-stub-idp"
RUN_DIR="../ida-stub-idp"
DIST_DIR="$RUN_DIR/build/distributions"
ZIP_DIR="$DIST_DIR/$APP_NAME"

pushd "$RUN_DIR" >/dev/null
  ./gradlew clean distZip
popd >/dev/null

pushd "$DIST_DIR" >/dev/null
  echo "Extracting distribution zip"
  unzip -q "${APP_NAME}.zip"
popd >/dev/null

echo "Running Stub IDP with Proxy Node config"
(CONFIG_FILE="$(pwd -P)/stub-idp/resources/local/configuration.yml" "$ZIP_DIR/bin/$APP_NAME" &) >/dev/null
