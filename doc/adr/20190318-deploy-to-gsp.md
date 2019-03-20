# Deploy proxy node on GDS Supported Platform

Date: 2019-03-18

## Status

Accepted

## Context

GDS requires that all GDS-run services are hosted and maintained internally and in a consistent manner. For microservice architectures such as the proxy node, the technology GDS has chosen is Kubernetes deployed to AWS instances, dubbed the GDS Supported Platform (GSP). Applications running on the platform are containerised using Docker.

The platform provides a number of features out of the box such as a continuous integration and delivery (CI/CD), monitoring, logging and networking for the hosted applications.

The proxy node architecture has the additional requirements of only deploying signed Docker images and restricting which applications can be co-located on the host machine, both of which can be provided by the GSP.

## Decision

The proxy node AWS infrastructure, including the Kubernetes deployment and CI system, will be built and managed by the GSP team. The developers will be responsible for building the applications and will entrust deployment to the GSP pipeline.

## Consequences

- The proxy node is one of the first architectures to be built by GSP
- The proxy node's AWS account will be administered by GSP
- Infrastructure issues will have to be handled by GSP
- Out of hours support responsibilities will have to be figured out
