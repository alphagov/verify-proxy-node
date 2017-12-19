#!/usr/bin/env bash

# Kill old services
./kill-service.sh 2>/dev/null

# Setup environment for local running
# Proxy Node
export PROXY_NODE_ENTITY_ID="https://dev-hub.local"
export HUB_URL="http://localhost:50140/stub-idp-demo/SAML2/SSO"

# Stub IDP
stub_idp_local="$(pwd -P)/stub-idp/resources/local"
export HUB_ENTITY_ID="$PROXY_NODE_ENTITY_ID"
export IDP_SIGNING_PRIVATE_KEY="$stub_idp_local/stub_idp_signing.pk8"
export IDP_SIGNING_CERT="$stub_idp_local/stub_idp_signing.crt"
export STUB_IDPS_FILE_PATH="$stub_idp_local/stub-idps.yml"
export METADATA_URL="http://localhost:6600/hub-metadata/local"
export METADATA_TRUST_STORE="$stub_idp_local/metadata.ts"
export METADATA_TRUST_STORE_PASSWORD="marshmallow"

# Start applications
source start-proxy-node.sh
source start-stub-idp.sh

until $(curl --output /dev/null --silent --head --fail http://localhost:6601/); do
  echo "Waiting for Proxy Node"
  sleep 1
done

until $(curl --output /dev/null --silent --head --fail http://localhost:50141/); do
  echo "Waiting for Stub IDP"
  sleep 1
done
