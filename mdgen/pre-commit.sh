#!/usr/bin/env bash

XMLSECTOOL=${XMLSECTOOL:-xmlsectool}

output=$(mktemp 2>/dev/null || mktemp -t 'mdgen')
log=$(mktemp 2>/dev/null || mktemp -t 'mdgen')

function cleanup {
  rm "$output" "$log"
}

trap cleanup EXIT

function test_with_file {
  local node="$1"
  local cert="$2"
  local key="$3"
  local algo="$4"
  local key_pass="1234"
  echo -n "test_with_file $node $cert $key $algo: "
  ./gradlew run -q --args "$node test/${node}.yml $cert --algorithm $algo --credential file --key-file $key --key-pass $key_pass --output $output" >"$log" 2>&1 \
    && $XMLSECTOOL --verifySignature --inFile "$output" --certificate "$cert" >"$log" 2>&1
  test 0 -eq "$?" && echo OK || {
    echo FAIL
    cat "$log"
    exit 1
  }
}

for node in proxy connector; do
  test_with_file "$node" "test/cert.rsa.pem" "test/key.rsa.pem" "rsa"
  test_with_file "$node" "test/cert.ecdsa.pem" "test/key.ecdsa.pem" "ecdsa"
done
