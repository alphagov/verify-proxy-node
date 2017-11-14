# 2. Continuous integration on Jenkins

Date: 2017-11-07

## Status

Accepted

## Context

The team needs a way to automatically test code changes that get pushed to our central repository on Github.
Successful builds should also trigger a deployment to PaaS.
Since we're working in a public Github repository, it should be possible to use the public Travis CI but that requires all our libraries to be published in a public place (such as Maven Central).
We're probably going to depend on our internal SAML libraries, which are public on Github but are not yet being publicly published.
Until that happens, we're pretty much limited to using our internal build Jenkins.

## Decision

All our continuous integration and deployment jobs will be on the internal CI Jenkins.
The job configuration will be managed by [Jenkins Job Builder](https://github.com/alphagov/verify-jenkins-job-builder).
The actions carried out by the jobs are actually checked into this repository as shell scripts under the `jenkins/` directory, allowing us to make changes to the job actions without making a change to the job configuration itself.

## Consequences

Three new Jenkins jobs have been created:

- *eidas-notification-build*: Run tests and build an artifact.
- *eidas-notification-pr*: Run tests on pull requests submitted to the repository (by team members only).
- *eidas-notification-deploy-to-paas*: Create a distribution ZIP and push to PaaS.
