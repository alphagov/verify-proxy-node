#!/usr/bin/env bash
set -ueo pipefail

PN_PROJECT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

docker-compose down
