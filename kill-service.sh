#!/usr/bin/env bash

# Kill locally running proxy node

jps | awk '/EidasProxyNodeApplication/ { print $1 }' | xargs kill
