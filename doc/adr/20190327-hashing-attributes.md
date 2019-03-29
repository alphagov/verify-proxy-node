# Hashing the identity information

Date: 2019-03-27

## Status

Accepted

## Context

The SAML profile used by the eIDAS scheme requires that SAML messages are sent directly to the receiving proxy node via a user's browser. This introduces a host of security concerns, especially when we take the history of SAML exploits and XML security in general into account. Due to many of the attacks being centred around breaking XML parsing, the main concerns raised were of SAML being parsed by a public-facing service or the service responsible for communicating with the hardware security module (HSM). With all this considered, the role of the SAML parsing has been assigned to the Verify Service Provider (VSP). 

We need to have confidence that the identity information we are sending back to the Member State is the same as the identity information which was originally issued by the identity provider (IDP). The Gateway service will receieve a SAML response back from the Hub which is sent to the Translator service, before being passed onto the VSP which will parse the response. Due to the exploits surrounding XML parsing, we need to be confident that the VSP hasn't been compromised, as it could result in manipulated attributes being sent to the Translator which would ultimately result in fraudulent identities being sent back to a Member State.

## Decision

To ensure that we are confident that the identity information we are sending back to the Member State is the same as what was originally issued by the IDP, we will LOG to monitoring a secure one-way SHA-256 hash of the user attributes in the Verify Hub and within the Translator service in the eIDAS Proxy Node. Any mismatches between the two Hashes will trigger an alert. 

## Consequences

- This would be more beneficial to be implemented as a technical control, to have a higher confidence that no malicious transactions are approved.