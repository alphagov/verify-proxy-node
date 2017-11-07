# 3. Deploy to PaaS

Date: 2017-11-07

## Status

Accepted

## Context

The proxy node should exist somewhere we can easily access for showcase and automated acceptance testing.
We don't want to maintain any infrastructure at this stage, and there is only one service being created for the time being.

## Decision

The proxy node service will be deployed and run on [PaaS](https://www.cloud.service.gov.uk/) which avoids having to deal with infra ourselves.
We will create a `notification` space under the `govuk-verify` org on PaaS.

## Consequences

Everyone on the team will need to get PaaS credentials and access to the `notification` space.
Contributors will have to download and setup the Cloudfoundry CLI tool.
A `manifest.yml` file has been added to the repository to define the PaaS app deploy.
