# Separation of concerns

Date: 2019-03-21

## Status

Accepted

## Context

The SAML profile used by the eIDAS scheme requires that SAML messages are sent directly to the receiving proxy node via a user's browser. This introduces a host of security concerns, especially when we take the history of SAML exploits and XML security in general into account. Due to many of the attacks being centred around breaking XML parsing, the main concerns raised were of SAML being parsed by a public-facing service or the service responsible for communicating with the hardware security module (HSM). This suggests we can't implement the proxy node with a monolithic architecture.

## Decision

The proxy node is going to be split out into a series of microservices, each with a very limited set of responsibililties. Kubernetes ensures that sensitive microservices do not run on the same node. An attacker is limited in the damage they can do by compromising a single service and should be unable to compromise a vital service using maliciously constructed XML.

### Gateway

Public internet-facing service hosting the SAML endpoints and responsible for managing the user session. No parsing of SAML is carried out by this service.

### eIDAS SAML Parser (ESP)

A small service solely responsible for parsing eIDAS SAML AuthnRequests and returning useful information in JSON format to the gateway for session management purposes. The ESP is also responsible for consuming and validating the requesting member state's connector node metadata and passing the encryption certificate back for use by the translator (via the user's session).

### Verify Service Provider (VSP)

An application provided by Verify to build Verify SAML AuthnRequests and parse SAML Responses from the Verify Hub to return a user's attributes in JSON format.

### Translator

Constructs the eIDAS SAML response destined for the requesting member state's connector node. The message is signed using the HSM and encrypted for the connector.

### Message flow

     ┌─────────┐                         ┌───────┐                ┌───┐          ┌───┐                 ┌──────────┐                             ┌───┐          ┌────┐
     │Connector│                         │Gateway│                │ESP│          │VSP│                 │Translator│                             │Hub│          │Logs│
     └────┬────┘                         └───┬───┘                └─┬─┘          └─┬─┘                 └────┬─────┘                             └─┬─┘          └─┬──┘
          │ 1 [Browser] SAML(eIDAS Request)  │                      │              │                        │                                     │              │
          │─────────────────────────────────>│                      │              │                        │                                     │              │
          │                                  │                      │              │                        │                                     │              │
          │                                  │2 JSON(eIDAS Request) │              │                        │                                     │              │
          │                                  │─────────────────────>│              │                        │                                     │              │
          │                                  │                      │              │                        │                                     │              │
          │                                  │ 3 JSON(useful info)  │              │                        │                                     │              │
          │                                  │<─────────────────────│              │                        │                                     │              │
          │                                  │                      │              │                        │                                     │              │
          │                                  │                 4    │              │                        │                                     │              │
          │                                  │────────────────────────────────────>│                        │                                     │              │
          │                                  │                      │              │                        │                                     │              │
          │                                  │       5 JSON(Verify Request)        │                        │                                     │              │
          │                                  │<────────────────────────────────────│                        │                                     │              │
          │                                  │                      │              │                        │                                     │              │
          │                                  │                      │          6 [Browser] SAML(Verify Request)                                   │              │
          │                                  │───────────────────────────────────────────────────────────────────────────────────────────────────>│              │
          │                                  │                      │              │                        │                                     │              │
          │                                  │                      │              │                        │                                     │ 7 <user hash>│
          │                                  │                      │              │                        │                                     │ ─────────────>
          │                                  │                      │              │                        │                                     │              │
          │                                  │                      │          8 [Browser] SAML(Verify Response)                                  │              │
          │                                  │<───────────────────────────────────────────────────────────────────────────────────────────────────│              │
          │                                  │                      │              │                        │                                     │              │
          │                                  │                   9 JSON(Verify Response)                    │                                     │              │
          │                                  │──────────────────────────────────────────────────────────────>                                     │              │
          │                                  │                      │              │                        │                                     │              │
          │                                  │                      │              │10 JSON(Verify Response)│                                     │              │
          │                                  │                      │              │<────────────────────────                                     │              │
          │                                  │                      │              │                        │                                     │              │
          │                                  │                      │              │   11 JSON(user data)   │                                     │              │
          │                                  │                      │              │────────────────────────>                                     │              │
          │                                  │                      │              │                        │                                     │              │
          │                                  │                      │              │                        │                   12 <user hash>    │              │
          │                                  │                      │              │                        │ ───────────────────────────────────────────────────>
          │                                  │                      │              │                        │                                     │              │
          │                                  │                      │              │                        │ 13 <sign Response> ┌───┐            │              │
          │                                  │                      │              │                        │ <─────────────────>│HSM│            │              │
          │                                  │                      │              │                        │                    └─┬─┘            │              │
          │                                  │                   14 JSON(eIDAS Response)                    │                                     │              │
          │                                  │<──────────────────────────────────────────────────────────────                      │              │              │
          │                                  │                      │              │                        │                      │              │              │
          │15 [Browser] SAML(eIDAS Response) │                      │              │                        │                      │              │              │
          │<─────────────────────────────────│                      │              │                        │                      │              │              │
          │                                  │                      │              │                        │                                     │              │

## Consequences

- The usual maintenance overhead of running microservices.
