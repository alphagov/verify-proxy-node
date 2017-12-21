#!/usr/bin/env bash

set -e

./jenkins/build.sh
./local-acceptance.sh