#!/usr/bin/env bash

proxy_node_pids="jps | awk '/EidasProxyNodeApplication/ { print \$1 }'"
stub_idp_pids="jps | awk '/StubIdpApplication/ { print \$1 }'"

# Kill locally running proxy node

printf "Killing Proxy Node ..."
while [ -n "$(eval $proxy_node_pids)" ]; do
  printf "."
  eval $proxy_node_pids | xargs kill 2>/dev/null
  sleep 1
done
echo "KILLED"

printf "Killing Stub IDP ..."
while [ -n "$(eval $stub_idp_pids)" ]; do
  printf "."
  eval $stub_idp_pids | xargs kill 2>/dev/null
  sleep 1
done
echo "KILLED"
