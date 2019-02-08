#!/usr/bin/env bash

HSM_MODULE=${HSM_MODULE:-/usr/lib/softhsm/libsofthsm2.so}

output=$(mktemp)
log=$(mktemp)

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
    && xmlsectool --verifySignature --inFile "$output" --certificate "$cert" >"$log" 2>&1
  test 0 -eq "$?" && echo OK || {
    echo FAIL
    cat "$log"
    exit 1
  }
}

function test_with_hsm {
  local node="$1"
  local cert="$2"
  local algo="$3"
  local key_pass="1234"
  echo -n "test_with_hsm  $node $cert $key $algo: "
  ./init_softhsm.sh "$algo" >"$log" 2>&1 \
    && ./gradlew run -q --args "$node test/${node}.yml $cert --algorithm $algo --credential hsm --hsm-module $HSM_MODULE --hsm-key-label $algo --hsm-pin 1234 --output $output" >"$log" 2>&1 \
    && xmlsectool --verifySignature --inFile "$output" --certificate "$cert" >"$log" 2>&1
  test 0 -eq "$?" && echo OK || {
    echo FAIL
    cat "$log"
    exit 1
  }
}

for node in proxy connector; do
  test_with_file "$node" "test/cert.rsa.pem" "test/key.rsa.pem" "rsa"
  test_with_file "$node" "test/cert.ecdsa.pem" "test/key.ecdsa.pem" "ecdsa"

  test_with_hsm "$node" "test/cert.rsa.pem" "rsa"
  test_with_hsm "$node" "test/cert.ecdsa.pem" "ecdsa"
done
