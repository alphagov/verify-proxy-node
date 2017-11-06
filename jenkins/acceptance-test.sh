#!/usr/bin/env bash

expected="Hello - Verified By Hub" 
response=$(curl --silent -X POST -d "Hello" https://verify-eidas-notification.cloudapps.digital/verify-uk)

echo "Expected: $expected"
echo "Actual: $response"
test "$expected" = "$response"
