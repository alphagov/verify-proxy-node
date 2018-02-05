#!/usr/bin/env bash

# pull down verify metadata

REMOTE="git@github.com:alphagov/verify-metadata.git"
APP_NAME="verify-metadata"
RUN_DIR="verify-metadata"

test -d "$RUN_DIR" || git clone --quiet --depth 1 "$REMOTE" "$RUN_DIR" 

pushd "$RUN_DIR"
  git pull --quiet
popd
