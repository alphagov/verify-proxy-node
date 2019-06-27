#!/usr/bin/env bash

# If a new configuration is added it will need adding to `snyk_configurations.sh`.
# This script is intended to be used by a CI server.

function print_banner() {
    echo "######################################################################"
    echo "### Monitoring dependencies for $1 gradle configuration"
    echo "######################################################################"
}

function monitor_configuration() {
    local config=$1;
    print_banner "$config"
    # The sub-project specified here is irrelevant. The configuration will still be tested regardless of if the
    # sub-project actually uses it.
    snyk monitor --gradle-sub-project=proxy-node-translator --project-name="$config"-config -- --configuration="$config"
}

# any special cases where snyk should ignore a known vulnerability
source snyk/special-cases.sh

source snyk/configurations.sh

for configuration in $CONFIGURATIONS; do
    monitor_configuration $configuration
done;