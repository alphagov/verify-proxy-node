#!/usr/bin/env bash

ENV=${1-}

TEST_EC_CERT='./.test-metadata/proxy_node_metadata_signing.crt'
TEST_EC_KEY='./.test-metadata/proxy_node_metadata_signing.p8'

export METADATA_SIGNING_KEY_LABEL="$ENV-metadata-signing"
export PKCS11_LIBRARY_HSM=/usr/local/lib/softhsm/libsofthsm2.so
export PKCS11_NAME_HSM=pkcs11-tool

function add_signing_keys_to_hsm() {

    $PKCS11_NAME_HSM \
        --module $PKCS11_LIBRARY_HSM \
        --init-token \
        --slot-index 0 \
        --so-pin 1234567 \
        --init-pin \
        --pin 123456 \
        --label $METADATA_SIGNING_KEY_LABEL

    $PKCS11_NAME_HSM \
        --module $PKCS11_LIBRARY_HSM \
        -p 123456 -l -w $TEST_EC_KEY \
        -y privkey \
        -a $METADATA_SIGNING_KEY_LABEL \
        -d AAAA \
        --usage-sign

    $PKCS11_NAME_HSM \
        --module $PKCS11_LIBRARY_HSM \
        -p 123456 -l -w $TEST_EC_CERT \
        -y cert \
        -a $METADATA_SIGNING_KEY_LABEL \
        -d AAAA
}

add_signing_keys_to_hsm