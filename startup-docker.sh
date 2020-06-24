#!/usr/bin/env bash
set -eu
trap 'cleanup' EXIT
pushd $(dirname "$0") > /dev/null

export USE_LOCAL_BUILD=true
export RUN_TESTS=${RUN_TESTS:-false}
export VERIFY_USE_PUBLIC_BINARIES=${VERIFY_USE_PUBLIC_BINARIES:-false}

cleanup() {
  [[ $? -gt 0 ]] && docker-compose down
  popd > /dev/null
}

usage() {
cat << Description
Options:
    -b, --build           (Re)build images and binaries. Gradle runs on the host and binary dirs are mounted in Docker
                          for execution. This is the default option
    -d, --build-in-docker (Re) build images and binaries. Source code is copied to the image and Gradle runs inside
                          Docker. Use this option if you want everything to run in Docker. This is slower than building
                          on the host and mounting the output in the container.
    -l, --local-hub       Configure the VSP to talk to a locally running Hub instead of the default remote Compliance Tool
    -r, --restart         Kill the currently running Proxy Node instance if there is one and start a new one.
Description
}

missing_binaries() {
  ! ls ./*/build/install/*/bin/* >/dev/null 2>&1 ||
  ! [ -x ../verify-service-provider/build/install/verify-service-provider/bin/verify-service-provider ]
}

build_images=false
if [[ "$*" =~ "--build" || "$*" =~ "-b" ]]; then build_images=true; fi
if [[ "$*" =~ "--build-in-docker" || "$*" =~ "-d" ]]; then build_images=true; export USE_LOCAL_BUILD=false; else show_logs=true; fi
if [[ "$*" =~ "--local-hub" || "$*" =~ "-l" ]]; then vsp=verify-service-provider-local-hub; else vsp=verify-service-provider; fi
if [[ "$*" =~ "--no-confirm" ]]; then interactive=false; else interactive=true; fi
if [[ "$*" =~ "--restart" || "$*" =~ "-r" ]]; then docker-compose down; fi
if [[ "$*" =~ "--help" ]]; then usage && exit 1; fi

if ! docker inspect $vsp > /dev/null 2>&1; then
  docker-compose rm -sf verify-service-provider verify-service-provider-local-hub > /dev/null
fi

if ${build_images} || (${USE_LOCAL_BUILD} && missing_binaries); then
  if ${USE_LOCAL_BUILD}; then
    echo "Building apps..." && ./gradlew --parallel installDist
    pushd ../verify-service-provider; ./gradlew --parallel installDist; popd
  fi

  echo "Building images..." && docker-compose build --parallel ${show_logs:+--quiet}
fi

echo "Starting Gateway, Translator, eIDAS SAML Parser, VSP..."
docker-compose up -d proxy-node-gateway translator eidas-saml-parser $vsp

# Stub Connector needs Proxy Node metadata
local-startup/wait-for-it.sh -n "Proxy Node Metadata" -u "http://localhost:6600/ServiceMetadata"

echo "Starting Stub Connector..."
docker-compose up -d --no-deps stub-connector

# Metatron needs Connector metadata
local-startup/wait-for-it.sh -n "Stub Connector" -u "http://localhost:6610/ConnectorMetadata"

echo "Starting Metatron..."
docker-compose up -d --no-deps metatron

# Metatron needs to initialise and fetch Connector Metadata
local-startup/wait-for-it.sh -n "Metatron" -u "http://localhost:6670/metadata/http%3A%2F%2Fstub-connector%3A6610%2FConnectorMetadata"

stub_connector_url="http://localhost:6610/"
echo "Go to "$stub_connector_url" to initiate a journey"
if ${interactive}; then
  echo "Press Enter to open the page in a web browser..."
  if read -t 4; then open $stub_connector_url; fi
fi
