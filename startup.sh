#!/usr/bin/env bash

set -euo pipefail

PN_PROJECT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
PN_SCRIPTS_DIR="${PN_PROJECT_DIR}"/local_eidas_reference/scripts
PKI_OUTPUT_DIR="${PN_PROJECT_DIR}"/local_eidas_reference/docker/pki
GATEWAY_PROJECT_DIR="${PN_PROJECT_DIR}"/gateway
TRANSLATOR_PROJECT_DIR="${PN_PROJECT_DIR}"/translator

follow=false

reference_rebuild=false
gateway_rebuild=false
translator_rebuild=false
stub_idp_rebuild=false

while [ ! $# -eq 0 ]
do
  case "$1" in
    --follow)
      follow=true
      ;;
    --reference-rebuild)
      reference_rebuild=true
      ;;
    --stub-idp-rebuild)
      stub_idp_rebuild=true
      ;;
    --gateway-rebuild)
      gateway_rebuild=true
      ;;
    --translator-rebuild)
      translator_rebuild=true
      ;;
    --build)
      reference_rebuild=true
      stub_idp_rebuild=true
      gateway_rebuild=true
      translator_rebuild=true
      ;;
    *)
      echo "Usage $0 [--follow --gateway-rebuild --translator-rebuild --stub-idp-rebuild --reference-rebuild]"
      exit 1
      ;;
  esac
  shift
done

pushd "${PN_PROJECT_DIR}"/local_eidas_reference
  source "${PN_SCRIPTS_DIR}"/setup-verify-metadata.sh
  source "${PN_SCRIPTS_DIR}"/setup-verify-eidas-reference.sh

  reference_scripts_dir="${PN_PROJECT_DIR}"/local_eidas_reference/verify-eidas-reference/scripts
  if [ "$stub_idp_rebuild" = true ]
  then
    test -d "verify-stub-idp-repo" || git clone --quiet --depth 1 "git@github.com:alphagov/verify-stub-idp" "verify-stub-idp-repo"
    pushd "verify-stub-idp-repo"
      git pull --quiet
      echo "rootProject.name = 'verify-stub-idp'" >> settings.gradle
      ./gradlew clean distZip -Pversion=local -PincludeDirs=configuration -x test
    popd
    rm -rf stub-idp
    unzip verify-stub-idp-repo/stub-idp/build/distributions/stub-idp-local.zip
    docker build -f docker/Dockerfile.stub-idp . -t notification-stub-idp
  fi

  if [ "$gateway_rebuild" = true ]
  then
    pushd "$PN_PROJECT_DIR"
      ./gradlew -p gateway clean distZip -x test
    popd
    rm -rf gateway
    unzip "${GATEWAY_PROJECT_DIR}/build/distributions/gateway.zip"
    docker build -f docker/Dockerfile.gateway . -t notification-gateway
  fi


  if [ "$translator_rebuild" = true ]
  then
    pushd "$PN_PROJECT_DIR"
      ./gradlew -p translator clean distZip -x test
    popd
    rm -rf translator
    unzip "${TRANSLATOR_PROJECT_DIR}/build/distributions/translator.zip"
    docker build -f docker/Dockerfile.translator . -t notification-translator
  fi

  if [ "$reference_rebuild" = true ]
  then
    pushd "$reference_scripts_dir"/../docker
      "$reference_scripts_dir"/../docker/build_docker_image.sh
    popd
  fi
popd

pushd "${PN_PROJECT_DIR}/pki"
  rm -f "${PKI_OUTPUT_DIR}/*"
  bundle install
  bundle exec generate \
    --hub-entity-id "https://dev-hub.local" \
    --idp-entity-id "http://stub_idp.acme.org/stub-idp-demo/SSO/POST" \
    --proxy-node-entity-id "_verify_proxy_node" \
    --hub-response-url "http://localhost:56018/SAML2/SSO/Response/POST" \
    --idp-sso-url "http://localhost:56017/stub-idp-demo/SAML2/SSO" \
    --proxy-sso-url "http://localhost:56016/SAML2/SSO/POST" \
    --env \
    "${PKI_OUTPUT_DIR}"
popd

pushd "${PN_PROJECT_DIR}"/local_eidas_reference/docker
  docker-compose up -d
  if [ "$follow" = true ]
  then
    docker-compose logs -f
  fi
popd
