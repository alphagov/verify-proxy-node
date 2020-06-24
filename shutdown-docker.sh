#!/usr/bin/env bash
set -eu

echo "Shutting down Proxy Node services..."
pushd $(dirname "$0") > /dev/null
docker-compose down 2>/dev/null
popd > /dev/null
echo "Done"