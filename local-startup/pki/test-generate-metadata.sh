#!/usr/bin/env bash

rm -rf ./.test-metadata/

bundle install --quiet

# Generate the cert files required for testing stand-alone metadata generation
# This still generates the local as-is metadata_for_hub.xml and metadata_for_connector_node.xml which can be used for comparison
bundle exec generate \
      ./test/.test-metadata \
      --files

# Run the stand-alone generator to output metadata.xml
bundle exec generate-proxy-node-metadata.rb \
      ./test/.test-metadata \
      --metadata-env local-test \
      --root_ca_cert ./test/.test-metadata/root_ca.crt \
      --metadata_signing_cert ./test/.test-metadata/proxy_node_metadata_signing.crt \
      --proxy_signing_cert ./test/.test-metadata/proxy_node_signing.crt \
      --proxy-url 'http://local.proxynode' \
      --proxy-node-entity-id 'local.proxynode' \
      --filename metadata2.xml
