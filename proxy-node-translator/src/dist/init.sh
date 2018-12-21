#!/usr/bin/env bash
#
#   Initialise and launch Translator app in container
#

set -eu

base64 --decode < /app/keys/SIGNING_PRIVATE_KEY_SOFTHSM > /app/signing_private_key.p8
base64 --decode < /app/keys/SIGNING_CERT > /app/signing_cert.crt

# Init softHSM slot
pkcs11-tool --module $SOFT_HSM_LIB_PATH --init-token --slot $SOFT_HSM_SIGNING_KEY_SLOT --so-pin $SOFT_HSM_SIGNING_KEY_SO_PIN --init-pin --pin $SOFT_HSM_SIGNING_KEY_PIN --label $SOFT_HSM_SIGNING_KEY_LABEL

# Load Signing Private Key
pkcs11-tool --module $SOFT_HSM_LIB_PATH -p $SOFT_HSM_SIGNING_KEY_PIN -l -w /app/signing_private_key.p8 -y privkey -a $SOFT_HSM_SIGNING_KEY_LABEL -d $SOFT_HSM_SIGNING_KEY_ID --usage-sign

# Load Signing Cert
pkcs11-tool --module $SOFT_HSM_LIB_PATH -p $SOFT_HSM_SIGNING_KEY_PIN -l -w /app/signing_cert.crt -y cert -a $SOFT_HSM_SIGNING_KEY_LABEL -d $SOFT_HSM_SIGNING_KEY_ID

# Run the Translator app
exec "bin/$COMPONENT" $@

