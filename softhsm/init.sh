#!/usr/bin/env bash

set -eu

soft_hsm_lib="/usr/local/lib/softhsm/libsofthsm2.so"
p11tool="pkcs11-tool --module $soft_hsm_lib"

base64 -d < /app/keys/SIGNING_PRIVATE_KEY_SOFTHSM > signing_private_key.p8
base64 -d < /app/keys/SIGNING_CERT | openssl x509 -text | tee signing_cert.crt

openssl x509 -outform DER < signing_cert.crt > signing_cert.der

# ensure token store dir exists
mkdir -p /var/lib/softhsm/tokens

# Init softHSM slot
$p11tool \
  --init-token \
  --slot $SOFT_HSM_SIGNING_KEY_SLOT \
  --label $SOFT_HSM_SIGNING_KEY_LABEL \
  --so-pin $SOFT_HSM_SIGNING_KEY_SO_PIN \
  --init-pin \
  --pin $SOFT_HSM_SIGNING_KEY_PIN

# Load Signing Private Key
$p11tool \
  --pin $SOFT_HSM_SIGNING_KEY_PIN \
  --write-object signing_private_key.p8 \
  --type privkey \
  --label $SOFT_HSM_SIGNING_KEY_LABEL \
  --id $SOFT_HSM_SIGNING_KEY_ID \
  --usage-sign

# Load Signing Cert
$p11tool \
  --pin $SOFT_HSM_SIGNING_KEY_PIN \
  --write-object signing_cert.der \
  --type cert \
  --label $SOFT_HSM_SIGNING_KEY_LABEL \
  --id $SOFT_HSM_SIGNING_KEY_ID

# Clean up PKI
rm signing_private_key.p8 signing_cert.*

# Copy the required library file
cp /usr/local/lib/libpkcs11-proxy.so /softhsm/

# Run pkcs11-proxy on port 5656
exec "/usr/local/bin/pkcs11-daemon" "$soft_hsm_lib" $@

