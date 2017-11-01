# eIDAS Notification

This repository contains the eIDAS Proxy node implementation 

## Running tests

To run the tests manually, execute: `./gradlew clean test`.
For the tests to run automatically every time you commit, install [pre-commit](https://pre-commit.com)
via Homebrew and run `pre-commit install` in this repo.

## How to start the eidas-proxy-node application

1. Run `./gradlew run` to start your application.
1. To check that your application is running POST to url `http://localhost:6600/verify-uk`

