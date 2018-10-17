# eIDAS Notification

This repository contains the eIDAS Proxy Node implementation. Before you get started, it's
a good idea to run `./setup-mac.sh` if you're on OSX.

## Running tests

1. To run the tests manually, execute: `./gradlew clean test`.
1. Test results are output to `./build/test-results`.

If the script spends a very long time "waiting for CEF SP", try the following:

* `./shutdown.sh`
* `docker system prune --all`
* `./startup.sh --build`
* `./pre-commit.sh`

## How to start the eidas-proxy-node application

1. Run `./gradlew run` to start your application.
1. To check that your application is running POST to url `http://localhost:6600/verify-uk`
1. To reach the front page of CEF, browse to `http://localhost:56000`

## How to start the eidas-proxy-node in a container with stub-idp and CEF eIDAS reference

* Run `./startup.sh`. Use the rebuild flags described below if this is your first time running it.

The following options are available:

    --follow                Will display logs in the console
    --proxy-node-rebuild    If you want/need to build/rebuild the proxy-node image
    --stub-idp-rebuild      If you want/need to build/rebuild the stub-idp image
    --reference-rebuild     If you want/need to build/rebuild the CEF reference service image 

## Clicking through a journey

0. Run `./automated_journey.rb` to see a journey in action

## Pushing to PaaS

1. Authenticate with PaaS using `cf login`
1. Run `./gradle pushToPaas`
