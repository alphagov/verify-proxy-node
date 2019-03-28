# Add service mesh for traceability

Date: 2019-03-20

## Status

Accepted

## Context

eIDAS project has been challenged with some security concerns provided by
National Cyber Security Centre (NCSC). One of these concerns was a possibility
for a connector application or the connection between given connector and the
user being compromised and allow for stolen request IDs impersonate or create
new identities.

Sadly, it's out of our control. We do however, have an obligation to monitor
actions that are going through the system, in order to prove our innocence.

We have made a presumption, that Hardware Security Module (HSM) logs would be
able to tell us when it has been used to sign a thing and attach it to some of
the logs that we're currently holding.

This isn't however true for the version of HSM that is provided to us by AWS.
We have managed to raise a feature request, which will not be coming for a
while.

## Decision

Reliability Engineering has proposed to implement tracing through the system
with the use of Service Mesh. This requires our services to pass around a
header in the requests. Cyber Security team, will then create some monitoring
around these logs and alert when a path of the request is not performed in a
correct order indicating the request is being injected. These logs would then
be compared with a size of a body sent through to the HSM and back, to
establish when that exact request has been signed.

This doesn't solve the problem entirely, but is good enough for now before we
receive an appropriate solution from AWS.

## Consequences

- Complexity around networking
- Additional burden to maintain

