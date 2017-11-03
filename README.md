# eIDAS Notification

This repository contains the eIDAS Proxy Node implementation. Before you get started, it's
a good idea to run `./setup-mac.sh` if you're on OSX.

## Running tests

1. To run the tests manually, execute: `./gradlew clean test`.
1. Test results are output to `./build/test-results`.

## How to start the eidas-proxy-node application

1. Run `./gradlew run` to start your application.
1. To check that your application is running POST to url `http://localhost:6600/verify-uk`

## Pushing to PaaS

1. Authenticate with PaaS using `cf login`
1. Run `./gradle pushToPaas`
