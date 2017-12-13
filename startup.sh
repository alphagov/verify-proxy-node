#!/usr/bin/env bash

# Start proxy-node locally

RUN_DIR=$(pwd -P)
DIST_DIR="$RUN_DIR/build/distributions"
ZIP_DIR="$DIST_DIR/verify-eidas-notification"

./gradlew clean distZip

pushd "$DIST_DIR" >/dev/null
  echo "Extracting distribution zip"
  unzip -q verify-eidas-notification.zip
popd >/dev/null

echo "Running Proxy Node"
(CONFIG_FILE="$ZIP_DIR/config.yml" "$ZIP_DIR/bin/verify-eidas-notification" &) >/dev/null
