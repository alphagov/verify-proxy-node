# eIDAS Proxy Node

This repository contains the eIDAS Proxy Node implementation, which consists of the following services:

* `proxy-node-gateway`
* `proxy-node-translator`
* `eidas-saml-parser`
* `stub-idp`
* `stub-connector`

The eIDAS Proxy Node also runs the [verify-service-provider](https://github.com/alphagov/verify-service-provider) to build Verify SAML AuthnRequests and parse SAML Responses from the Verify Hub.

The eIDAS Proxy Node does not perform matching.

## Architectural Descision Records and documentation

We record our architectural decisions in `doc/adr`

A technical overview of the Proxy Node is available [here](doc/overview.md).

## Running the proxy node services

#### Preparing your environment (MacOS)
`./Brewfile` defines system dependencies for this project, notably Docker, Minikube and Kubernetes.
First install [homebrew](https://brew.sh/), then run
`brew bundle` 
to install these dependencies.

This will allow `minikube` to manage a `virtualbox` 
VM containing a Kubernetes cluster of the eIDAS Proxy Node services.

#### Startup
1. Run `./startup.sh`
1. Visit `http://$(minikube ip):31100/Request` to start a journey from `stub-connector`.

#### Troubleshooting the minikube cluster
* use `startup.sh` to rebuild services which have changed, in preference to using `shutdown.sh` then `startup.sh`.
The latter will rebuild all services and possibly reassign the minikube ip address.
* view logs on the VM with `minikube logs`
* ssh onto the VM with `minikube ssh`
* if minikube will not initialise the VM, run: `./shutdown.sh`, and remove `~/.minikube` directory.
* to use hyperkit in preference to virtualbox, first [install hyperkit ](https://github.com/kubernetes/minikube/blob/master/docs/drivers.md#hyperkit-driver), then run:
    ```
    minikube stop
    minikube config set vm-driver hyperkit
    ```

## Running unit tests

1. To run the tests manually, execute: `./gradlew clean test`.
1. Test results are output to `./build/test-results`.  

## License

[MIT](https://github.com/alphagov/verify-proxy-node/blob/master/LICENCE)
