# eIDAS Notification

This repository contains the eIDAS Proxy Node implementation. Before you get started, it's
a good idea to run `brew bundle` if you're on OSX. Also, to build the applications you'll
need to be on the GDS VPN.

## Running unit tests

1. To run the tests manually, execute: `./gradlew clean test`.
1. Test results are output to `./build/test-results`.

## How to start the proxy node services

1. Run `./startup.sh`
1. Visit `http://localhost:5000/Request` to start a journey from `stub-connector`.

## How to do a Kubernetes locally

1. `minikube start`
1. Run `./startup.sh`
1. Apply the `ConfigMap`s containing PKI-related things: `kubectl apply -R -f .local_pki/`.
1. Apply the configuration of the deployments and services: `kubectl apply -R -f yaml/`.
1. Visit `http://$(minikube ip):31100/Request`.

NB. The first journey seems to fail.
