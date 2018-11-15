#!/usr/bin/env bash

if [ "$1" == "--no-browser" ]
then
    SHOW_BROWSER=false
fi

set -eu

bundle --quiet
mkdir -p testreport
SHOW_BROWSER=${SHOW_BROWSER:-"true"} TEST_ENV=local bundle exec cucumber --strict