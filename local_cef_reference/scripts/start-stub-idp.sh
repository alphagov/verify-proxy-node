#!/usr/bin/env bash

APP_NAME="app"
RUN_DIR=$(pwd -P)
DIST_DIR="$RUN_DIR/build/distributions"
ZIP_DIR="$DIST_DIR/$APP_NAME"

CONFIG_FILE="$RUN_DIR/notification_resources/configuration.yml"

pushd "$RUN_DIR" >/dev/null
  ./gradlew clean distZip
popd >/dev/null

pushd "$DIST_DIR" >/dev/null
  echo "Extracting distribution zip"
  unzip -q "${APP_NAME}.zip"
popd >/dev/null

CONFIG_FILE=$CONFIG_FILE "$ZIP_DIR/bin/$APP_NAME" || sleep 12000
