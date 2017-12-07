#!/usr/bin/env bash

set -eu

. ./jenkins/login_to_paas.sh

./gradlew -x test pushToPaas
./jenkins/acceptance-test.sh
