#!/usr/bin/env bash

# Kill locally running proxy node

printf "Killing Proxy Node ... "
while jps | awk '/EidasProxyNodeApplication/ { print $1 }' | xargs kill 2>/dev/null; do 
  sleep 1
done
echo "KILLED"

printf "Killing Stub IDP ... "
while jps | awk '/StubIdpApplication/ { print $1 }' | xargs kill 2>/dev/null; do
  sleep 1
done
echo "KILLED"
