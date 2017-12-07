#!/usr/bin/env bash

./jenkins/login_to_paas.sh

VERIFY_EIDAS_NOTIFICATION_REPO=$(pwd)
MANIFEST_FILE=$VERIFY_EIDAS_NOTIFICATION_REPO/stub-idp/manifest.yml
STUB_IDP_RESOURCES=$VERIFY_EIDAS_NOTIFICATION_REPO/stub-idp/resources/paas

cd ../ida-stub-idp
./gradlew -x test \
      pushToPaas \
      -PmanifestFile=$MANIFEST_FILE \
      -PincludeDirectory=$STUB_IDP_RESOURCES
