#!/usr/bin/env bash

set -eu

export RUN_TESTS=${RUN_TESTS:-false}
export USE_LOCAL_BUILD=${USE_LOCAL_BUILD:-true}
export VERIFY_USE_PUBLIC_BINARIES=${VERIFY_USE_PUBLIC_BINARIES:-false}

if [[ "$*" =~ "--build" ]]; then BUILD_IMAGES=true; export USE_LOCAL_BUILD=true; else BUILD_IMAGES=false; fi
if [[ "$*" =~ "--local-hub" ]]; then VSP=verify-service-provider-local-hub; else VSP=verify-service-provider; fi

if ${BUILD_IMAGES}; then
  echo "Building apps..." && ./gradlew --parallel installDist
  echo "Building images..." && docker-compose build --parallel
fi

echo "Starting Gateway, Translator, Stub Connector, VSP..."
docker-compose up -d stub-connector proxy-node-gateway translator $VSP

# ESP won't start without Connector metadata
printf "Waiting for Stub Connector"
until $(curl --output /dev/null --silent --head --fail http://localhost:6610/ConnectorMetadata); do
  printf '.'
  sleep 2
done
echo

echo "Starting eIDAS SAML Parser and Metatron..."
docker-compose up -d eidas-saml-parser metatron

echo "Go to http://localhost:6610/ to initiate a journey"
