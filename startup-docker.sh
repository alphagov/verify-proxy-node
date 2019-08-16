#!/usr/bin/env bash

set -eu

export RUN_TESTS=${RUN_TESTS:-false}
export USE_LOCAL_BUILD=${USE_LOCAL_BUILD:-true}
export VERIFY_USE_PUBLIC_BINARIES=${VERIFY_USE_PUBLIC_BINARIES:-false}

BUILD_IMAGES=false; if [ "${1:-}" = "--build" ]; then BUILD_IMAGES=true; export USE_LOCAL_BUILD=true; fi

if ${BUILD_IMAGES}; then
  echo "Building apps..." && ./gradlew --parallel installDist
  echo "Building images..." && docker-compose build --parallel
fi

echo "Starting Gateway, Translator, Stub Connector, VSP..."
docker-compose up -d stub-connector proxy-node-gateway translator verify-service-provider

# ESP won't start without Connector metadata
printf "Waiting for Stub Connector"
until $(curl --output /dev/null --silent --head --fail http://localhost:6610/ConnectorMetadata); do
  printf '.'
  sleep 2
done
echo

echo "Starting eIDAS SAML Parser..."
docker-compose up -d eidas-saml-parser

echo "Go to http://localhost:6610/ to initiate a journey"
