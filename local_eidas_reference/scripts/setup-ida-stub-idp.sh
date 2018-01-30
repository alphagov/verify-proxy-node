#!/usr/bin/env bash

# pull down stub-idp

REMOTE="git@github.com:alphagov/ida-stub-idp"
APP_NAME="ida-stub-idp"
RUN_DIR="ida-stub-idp"

test -d "$RUN_DIR" || git clone --quiet --depth 1 "$REMOTE" "$RUN_DIR" 

