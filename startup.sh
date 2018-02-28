#!/usr/bin/env bash

set -euo pipefail

PN_PROJECT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
PN_SCRIPTS_DIR="${PN_PROJECT_DIR}"/local_eidas_reference/scripts
PKI_OUTPUT_DIR="${PN_PROJECT_DIR}"/local_eidas_reference/docker/pki

follow=false

reference_rebuild=false
proxy_node_rebuild=false
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
		--proxy-node-rebuild)
      proxy_node_rebuild=true
			;;
    --build)
      reference_rebuild=true
      stub_idp_rebuild=true
      proxy_node_rebuild=true
      ;;
		*)
      echo "Usage $0 [--follow --proxy-node-rebuild --stub-idp-rebuild --reference-rebuild]"
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
    test -d "ida-stub-idp-repo" || git clone --quiet --depth 1 "git@github.com:alphagov/ida-stub-idp" "ida-stub-idp-repo"
    pushd "ida-stub-idp-repo"
      git pull --quiet
      echo "rootProject.name = 'ida-stub-idp'" > settings.gradle
      ./gradlew clean distZip -Pversion=local -PincludeDirs=configuration -x test
    popd
    rm -rf ida-stub-idp
    unzip ida-stub-idp-repo/build/distributions/ida-stub-idp-local.zip
    docker build -f docker/Dockerfile.stub-idp . -t notification-stub-idp
  fi

  if [ "$proxy_node_rebuild" = true ]
  then
    pushd "$PN_PROJECT_DIR"
      ./gradlew clean distZip -x test
    popd
    rm -rf verify-eidas-proxy-node
    unzip "${PN_PROJECT_DIR}/build/distributions/verify-eidas-proxy-node.zip"
    docker build -f docker/Dockerfile.proxy-node . -t notification-proxy-node
  fi

  if [ "$reference_rebuild" = true ]
  then
    "$reference_scripts_dir"/build_docker_image.sh
  fi
popd

pushd "${PN_PROJECT_DIR}/pki"
  rm -f "${PKI_OUTPUT_DIR}/*"
  bundle install
  bundle exec generate \
    --hub-entity-id "https://dev-hub.local" \
    --idp-entity-id "http://stub_idp.acme.org/stub-idp-demo/SSO/POST" \
    --proxy-node-entity-id "_verify_proxy_node" \
    --hub-response-url "http://localhost:56016/SAML2/SSO/Response/POST" \
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

