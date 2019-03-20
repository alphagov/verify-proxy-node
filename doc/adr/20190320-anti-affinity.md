# Defining Anti-Affinity for Gateway and Translator services

Date: 2019-03-20

## Status

Accepted

## Context

eIDAS project has been challenged with some security concerns provided by
National Cyber Security Centre (NCSC). One of these concerns was a hypothesis
that an attacker could compromise the public facing service (Gateway) by
providing insecure SAML, could perform a Kernel Attack against the more fragile
and sensitive but not exposed service (Translator) which has capability to
interacting with Hardware Security Module (HSM).

Provided we're running on GDS Supported Platform (GSP), Reliability Engineering
(RE) team has suggested separating any service capable of interacting with HSM
into a different physical machine. This should prevent this sort of attack from
happening.

## Decision

We'll trust that the GSP is be capable of deploying sensitive applications away
from the ones that are interacting with HSM.

## Consequences

- Additional complicity we need to keep in mind

