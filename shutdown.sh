#!/usr/bin/env bash
set -ueo pipefail

PN_PROJECT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

pushd "${PN_PROJECT_DIR}"/local_eidas_reference/docker
  docker-compose down
popd
