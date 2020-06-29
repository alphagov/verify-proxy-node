#!/usr/bin/env bash
set -eu

export TEST_ENV=${TEST_ENV:-local}
envs=(local build integration)
started_local_node=false
started_selenium_hub=false
reuse_selenium_hub=false
run_in_docker=false

usage() {
  echo "Usage: [-e environment] [-b] [-c] [-d] [-s]"
  echo "   -b   Show browser. Doesn't work with -d"
  echo "   -c   Use Chrome instead of the default Firefox. Doesn't work with -d"
  echo "   -d   Run tests in a Docker container (doesn't work with local env)"
  echo "   -e   Specify environment to run tests against. Possible values are [${envs[@]}]. Default is local"
  echo "   -s   Don't kill Selenium Hub so it can be reused when running with Docker (-d)"
  exit 1
}

while getopts "bcde:s" OPT; do
  case $OPT in
  b)
    export SHOW_BROWSER=true
    ;;
  c)
    export BROWSER=chrome
    ;;
  d)
    run_in_docker=true
    ;;
  e)
    if ! [[ "${envs[*]}" =~ "$OPTARG" ]]; then echo "Invalid environment" && usage; fi
    export TEST_ENV=$OPTARG
    ;;
  s)
    reuse_selenium_hub=true
    ;;
  *)
    usage
    ;;
  esac
done

if [[ "$TEST_ENV" == "local" ]] && ${run_in_docker}; then
  echo "Can't run tests in Docker against local env because the Hub only runs on localhost and this causes issues with SAML metadata."
  echo "Select a different environment or run tests without Docker" && echo && usage
fi

echo "Running acceptance tests for environment $TEST_ENV"
pushd $(dirname "$0") > /dev/null

if [[ "$TEST_ENV" == "local" ]]; then
  if ! ps aux | grep java | grep saml-engine.yml > /dev/null 2>&1; then
    echo "The Hub needs to be running for local tests. Start the Hub and the Proxy Node first." && exit 1
  fi

  if ! docker network inspect verify-proxy-node > /dev/null 2>&1; then
    echo "Starting Proxy Node..."
    started_local_node=true; pushd .. > /dev/null; ./startup-docker.sh --build --local-hub --no-confirm; popd > /dev/null
  else
    if ! docker inspect verify-service-provider-local-hub > /dev/null 2>&1; then
      echo "Proxy Node needs to be running against the local Hub to run local tests"
      echo "Start the Hub first and then ./startup-docker.sh --local-hub" && exit 1
    fi
  fi
fi

rm -rf testreport/

if ${run_in_docker} && [[ "$TEST_ENV" != "local" ]]; then
  if ! [[ $(docker ps -q -f status=running -f name=selenium-hub) ]]; then
    echo "Starting Selenium Hub..."
    started_selenium_hub=true; docker-compose up -d selenium-hub
    ../local-startup/wait-for-it.sh -n "Selenium Hub" -u "http://localhost:4444/wd/hub" --no-head
  fi

  docker-compose run acceptance-tests || exit_code=$?
else
  bundle exec cucumber --color --strict --tags "not @ignore" features/acceptance/ || exit_code=$?
fi

exit_code=${exit_code:-$?}

if ${started_selenium_hub} && ! ${reuse_selenium_hub}; then docker-compose down; fi

if ${started_local_node}; then
  echo "Stopping Proxy Node..."
  pushd .. > /dev/null; docker-compose down; popd > /dev/null
fi

popd > /dev/null
exit $exit_code