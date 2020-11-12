[![Build Status](https://travis-ci.com/alphagov/verify-proxy-node.svg?branch=master)](https://travis-ci.com/alphagov/verify-proxy-node)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/a24b4d2c6a834006a3c06fb8d7c47164)](https://www.codacy.com/app/alphagov/verify-proxy-node?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=alphagov/verify-proxy-node&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/a24b4d2c6a834006a3c06fb8d7c47164)](https://www.codacy.com/app/alphagov/verify-proxy-node?utm_source=github.com&utm_medium=referral&utm_content=alphagov/verify-proxy-node&utm_campaign=Badge_Coverage)

# eIDAS Proxy Node

This repository contains the eIDAS Proxy Node implementation, which supplies UK eIDs to notified EU Member States, according to the [eIDAS Regulation](https://ec.europa.eu/cefdigital/wiki/download/attachments/82773108/eIDAS%20Interoperability%20Architecture%20v.1.2%20Final.pdf?version=3&modificationDate=1571068651790&api=v2).

The UK Proxy Node is a service located between an EU Member State Connector Node, and a UK eID provider (Verify Hub). The Proxy Node is responsible for:

* transforming [eIDAS SAML requests](https://ec.europa.eu/cefdigital/wiki/download/attachments/82773108/eidas_message_format_v1.0.pdf?version=1&modificationDate=1497252920416&api=v2) from an EU Connector Node to Verify [SAML](https://en.wikipedia.org/wiki/SAML_2.0) requests
* transforming [eIDAS SAML responses](https://ec.europa.eu/cefdigital/wiki/download/attachments/82773108/eidas_message_format_v1.0.pdf?version=1&modificationDate=1497252920416&api=v2) from Verify SAML responses
* validating eIDAS SAML requests
* signing and encrypting eIDAS SAML responses

The Proxy Node consists of the following services:

| Service                 	| Role                                                                                 	|
|-------------------------	|--------------------------------------------------------------------------------------	|
| gateway                 	| public facing gateway that accepts and provides eIDAS SAML                           	|
| eidas-saml-parser       	| parses and validates eIDAS SAML requests from EU Member States                       	|
| verify-service-provider 	| creates and signs Verify SAML request payloads and parses, decrypts and validates Hub SAML responses 	|
| metatron                	| provides data on how to connect to country connector nodes                                         	|
| translator              	| creates signed and encrypted eIDAS SAML responses for EU Member States               	|
| stub-connector          	| represents a country connector node, for testing                              	|

The eIDAS Proxy Node does not perform [matching](https://www.docs.verify.service.gov.uk/using-verify-data/about-matching/#introduction-to-matching).

## Architectural Decision Records and documentation

We record our architectural decisions in `doc/adr`

A technical overview of the Proxy Node is available [here](doc/overview.md).

## Running the proxy node services

### Running locally with Docker

See [instructions](local-startup/running-proxy-node-locally.md) to run the Proxy Node locally with minimal set-up using Docker.

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
