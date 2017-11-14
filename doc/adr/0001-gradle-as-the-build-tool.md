# 1. Gradle as the build tool

Date: 2017-11-07

## Status

Accepted

## Context

We had to decide between using Maven and Gradle as our build tool.
The Dropwizard documentation for getting started with a new project suggests using Maven and it is initially easier to set up.
After a short time using it, however, we found a couple of issues:

- Difficult to override the Maven Central repository with our own (Artifactory).
- Required many new entries in the Artifactory whitelist for Maven plugins.
- Maven is unable to build the right kind of artifact for deploying to PaaS without extensive configuration.

## Decision

Gradle was chosen as our build tool for the following reasons:

- Experience with Gradle on the team.
- Compatibility with Artifactory.
- Ability to build a distribution ZIP for uploading to PaaS out of the box.

## Consequences

The gradle wrapper (`gradlew`) is checked into the repository. The `build.gradle` provides the following tasks:

- `./gradlew clean build`: Run tests and build a JAR.
- `./gradlew clean distZip`: Run tests and build a distribution ZIP.
- `./gradlew pushToPaas`: Do the above and publish to PaaS.
