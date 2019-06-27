#!/usr/bin/env bash

# Short-term rules for any special cases that snyk should ignore.
# Think hard about how long to allow the ignore to last.

# snyk should ignore this vulnerability in jackson-databind for 1 week:
# https://snyk.io/vuln/SNYK-JAVA-COMFASTERXMLJACKSONCORE-450917
# "A fix was pushed into the master branch but not yet published"
echo '[snyk/special-cases.sh] ignoring a known vuln in jackson-databind until 2019-07-04'
snyk ignore --id='SNYK-JAVA-COMFASTERXMLJACKSONCORE-450917' --expiry='2019-07-04' --reason='¯\_(ツ)_/¯ until jackson next updates, then we should update the version in gradle to latest'

# add further rules here