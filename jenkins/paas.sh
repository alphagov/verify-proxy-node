#!/usr/bin/env bash

./jenkins/login_to_paas.sh

./gradlew -x test pushToPaas
./jenkins/acceptance-test.sh
