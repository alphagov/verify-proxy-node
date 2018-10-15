#!/usr/bin/env bash

# pull down stub-idp

REMOTE="git@github.com:alphagov/verify-stub-idp"
APP_NAME="verify-stub-idp"
RUN_DIR="verify-stub-idp"

test -d "$RUN_DIR" || git clone --quiet --depth 1 "$REMOTE" "$RUN_DIR"

pushd "$RUN_DIR"
  git pull --quiet
popd
