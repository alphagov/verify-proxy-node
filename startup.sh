#!/usr/bin/env bash

PN_PROJECT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
PN_SCRIPTS_DIR="${PN_PROJECT_DIR}"/local_eidas_reference/scripts

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
  source "${PN_SCRIPTS_DIR}"/setup-ida-stub-idp.sh

  reference_scripts_dir=/local_eidas_reference/verify_eidas_reference/scripts

  if [ "$stub_idp_rebuild" = true ]
  then
    docker build -f docker/Dockerfile.stub-idp "${PN_PROJECT_DIR}" -t notification-stub-idp
  fi

  if [ "$proxy_node_rebuild" = true ]
  then
    docker build -f docker/Dockerfile.proxy-node "${PN_PROJECT_DIR}" -t notification-proxy-node
  fi

  if [ "$reference_rebuild" = true ]
  then
    "$reference_scripts_dir"/build_docker_image.sh
  fi
popd

pushd "${PN_PROJECT_DIR}"/local_eidas_reference/docker
  docker-compose up -d
  if [ "$follow" = true ]
  then
    docker-compose logs -f
  fi
popd

