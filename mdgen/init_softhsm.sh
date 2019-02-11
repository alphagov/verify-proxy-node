#!/usr/bin/env bash

# Clear all softhsm slots
softhsm2-util --show-slots | awk '/Serial number/ {print $NF}' | xargs -n1 softhsm2-util --delete-token --serial || :

# Initialise the softhsm with a certain keypair
algo="$1"
softhsm2-util --init-token --label "$algo" --so-pin 1234 --pin 1234 --free
softhsm2-util --token "$algo" --pin 1234 --import "test/key.${algo}.pk8" --label "${algo}" --id aa
openssl x509 -outform DER < "test/cert.${algo}.pem" | \
pkcs11-tool --module /usr/lib/softhsm/libsofthsm2.so \
  --token-label "$algo" \
  --pin 1234 \
  --write-object /dev/stdin \
  --type cert \
  --id aa \
  --label "${algo}"
