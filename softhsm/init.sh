#!/usr/bin/env bash

soft_hsm_lib="/usr/local/lib/softhsm/libsofthsm2.so"

base64 -d < /keys/SIGNING_PRIVATE_KEY_SOFTHSM > signing_private_key.p8
base64 -d < /keys/SIGNING_CERT > signing_cert.crt

# ensure token store dir exists
mkdir -p /var/lib/softhsm/tokens

# Init softHSM slot
pkcs11-tool --module $soft_hsm_lib --init-token --slot $SOFT_HSM_SIGNING_KEY_SLOT --so-pin $SOFT_HSM_SIGNING_KEY_SO_PIN --init-pin --pin $SOFT_HSM_SIGNING_KEY_PIN --label $SOFT_HSM_SIGNING_KEY_LABEL

# Load Signing Private Key
pkcs11-tool --module $soft_hsm_lib -p $SOFT_HSM_SIGNING_KEY_PIN -l -w signing_private_key.p8 -y privkey -a $SOFT_HSM_SIGNING_KEY_LABEL -d $SOFT_HSM_SIGNING_KEY_ID --usage-sign

# Load Signing Cert
pkcs11-tool --module $soft_hsm_lib -p $SOFT_HSM_SIGNING_KEY_PIN -l -w signing_cert.crt -y cert -a $SOFT_HSM_SIGNING_KEY_LABEL -d $SOFT_HSM_SIGNING_KEY_ID

# Clean up PKI
rm signing_private_key.p8 signing_cert.crt

# Copy the required library file
cp /usr/local/lib/libpkcs11-proxy.so /softhsm/

# Run pkcs11-proxy on port 5656
exec "/usr/local/bin/pkcs11-daemon" "$soft_hsm_lib" $@

