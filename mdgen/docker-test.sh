#!/usr/bin/env bash

docker build -t mdgen-test -f test.Dockerfile .
docker run --rm -v $PWD/src:/mdgen/src mdgen-test
