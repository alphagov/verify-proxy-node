#!/usr/bin/env bash
set -ueo pipefail

# pull down verify-eidas-reference

REMOTE="git@github.com:alphagov/verify-eidas-reference.git"
RUN_DIR="verify-eidas-reference"
SCRIPTS_DIR="$RUN_DIR"/scripts

test -d "$RUN_DIR" || ( echo "clone ${REMOTE} To ${RUN_DIR}..." && git clone --quiet --depth 1 "$REMOTE" "$RUN_DIR" )

pushd "$RUN_DIR"
  git pull --quiet
popd

if [ "${RECOMPILE:-false}" = true ]
then
  REBUILD=true
  "$SCRIPTS_DIR"/compile.sh
fi

if [ "${REBUILD:-false}" = true ]
then
  "$SCRIPTS_DIR"/build_docker_image.sh
fi

cp $RUN_DIR/../verify-metadata/signed/local-connector/metadata.xml $RUN_DIR/metadata-proxy/metadata.xml || (echo "Could not find signed metadata file" && exit 1)
