[![Build Status](https://travis-ci.com/alphagov/verify-proxy-node.svg?branch=master)](https://travis-ci.com/alphagov/verify-proxy-node)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/a24b4d2c6a834006a3c06fb8d7c47164)](https://www.codacy.com/app/alphagov/verify-proxy-node?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=alphagov/verify-proxy-node&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/a24b4d2c6a834006a3c06fb8d7c47164)](https://www.codacy.com/app/alphagov/verify-proxy-node?utm_source=github.com&utm_medium=referral&utm_content=alphagov/verify-proxy-node&utm_campaign=Badge_Coverage)

# eIDAS Proxy Node

This repository contains the eIDAS Proxy Node implementation, which consists of the following services:

* `proxy-node-gateway`
* `proxy-node-translator`
* `eidas-saml-parser`
* `stub-idp`
* `stub-connector`

The eIDAS Proxy Node also runs the [verify-service-provider](https://github.com/alphagov/verify-service-provider) to build Verify SAML AuthnRequests and parse SAML Responses from the Verify Hub.

The eIDAS Proxy Node does not perform matching.

## Architectural Decision Records and documentation

We record our architectural decisions in `doc/adr`

A technical overview of the Proxy Node is available [here](doc/overview.md).

## Running the proxy node services

### Running locally with Docker

See [instructions](local-startup/running-proxy-node-locally.md) to run the Proxy Node locally with minimal set-up using Docker.

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

## Snyk
Snyk is run by our Travis builds and does two things, test and monitor. We use the CLI rather than the GitHub integration as the integration has big problems with multi project Gradle builds like we have.

#### Test
* After the tests are run in the build Travis checks all our dependencies against a database for known vulnerabilities.
* If any are found it exists with a non-zero code and the build fails. The build logs tell you what happened.
* If no vulnerabilities are found then we move on to monitoring.

#### Monitor
* Snyk sends a list of all our dependencies to their server, and will alert us via email if any new vulnerabilities are found for them.
* The vulnerabilities can be found in [our Snyk dashboard](https://app.snyk.io/org/verify-eidas)
* You can be added to the Snyk verify-eidas organisation by an existing member.

#### Troubleshooting Snyk
* The most common issue is a build failing due to a new vulnerability. Follow the link in the logs, or visit the dashboard and see what's up.
* Most issues will have a resolution strategy. Most often you'll need to bump a library verion. This can also be a transitive dependency. Good luck.
* If there is currently no solution, you can temporarily ignore the vulnerability. You'll need the Snyk ID of the issue which you can grab from the last segment of the URL of the issue - find it in the Travis build logs where the vulnerability is reported.
* Run `snyk ignore --id=<IssueID> --reason='The reason you're ignoring it'` and commit the `.snyk` file generated. Push.


## License

[MIT](https://github.com/alphagov/verify-proxy-node/blob/master/LICENCE)
