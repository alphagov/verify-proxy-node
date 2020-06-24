#!/usr/bin/env bash
set -eu

timeout=30
elapsed=0

usage() {
  echo "Usage: -u url_to_await [-n service_name] [-t timeout] [--no-head]" && exit 1
}

while getopts "n:t:u:-:" OPT; do
  case $OPT in
  n)
    name=${OPTARG}
    ;;
  t)
    timeout=${OPTARG}
    ;;
  u)
    url=${OPTARG}
    ;;
  -)
    ;;
  *)
    usage
    ;;
  esac
done

if [[ -z "$url" ]]; then usage; fi
if [[ -z "$name" ]]; then name=$url; fi
if ! [[ "$*" =~ "--no-head" ]]; then head="--head"; fi

printf "Waiting for $name"
until curl --output /dev/null --silent ${head:-} --fail "$url"; do
  printf '.'
  sleep 2
  ((elapsed+=2))
  if [[ $elapsed -gt $timeout ]]; then echo "timed out" && exit 1; fi
done
echo