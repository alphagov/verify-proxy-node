#!/usr/bin/env bash

# Start proxy-node locally

APP_NAME="verify-eidas-notification"
RUN_DIR=$(pwd -P)
DIST_DIR="$RUN_DIR/build/distributions"
ZIP_DIR="$DIST_DIR/$APP_NAME"

pushd "$RUN_DIR" >/dev/null
  ./gradlew clean distZip
popd >/dev/null

pushd "$DIST_DIR" >/dev/null
  echo "Extracting distribution zip"
  unzip -q "${APP_NAME}.zip"
popd >/dev/null

echo "Running Proxy Node"

if [ "$DEBUG" = "true" ]; then
  echo "Running in Debug mode..."
  export JAVA_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5006"
fi

(CONFIG_FILE="$ZIP_DIR/config.yml" "$ZIP_DIR/bin/$APP_NAME" &) >/dev/null
