# Deploy a proxy node per connector node

Date: 2019-03-18

## Status

Accepted

## Context

The eIDAS specification states that member states (MS) requesting an eIDAS identity can operate one or more connector nodes. The MS exporting the identity, us in this case, should only operate one proxy node from the point of view of the requesting MS.

Trust between the connector node and proxy node is established by the connector consuming and verifying the proxy node's SAML metadata and vice versa. Verification of the metadata is accomplished using public keys exchanged by both parties out of band.

Therefore, from every MS that wishes to consume UK identities we will need: 
- the URL from which to fetch their metadata
- a certificate which can be used to verify the signature of their metadata
 
In turn, we will provide our URL and certificate to the requesting MS.

## Decision

Running a single instance of the proxy node services and infrastructure to handle requests from all potential requesting MS would need some way of aggregating their metadata. An additional service would most likely be required to do this, bringing with it the added cost of having to secure and administer that service, as it would end up being the root of trust for _all_ requesting MS. In our current implementation, a single proxy node would also mean a single signing key being used to export all UK identities to every requesting MS, making that key all the more valuable.

Instead, we will run one instance of the proxy node services and infrastructure isolation _per_ requesting MS connector node. They will share a single hardware security module (HSM) but will each have independent credentials to access the HSM and an independent signing key held by the HSM.

Having multiple instances running, one per requesting MS, would be fairly easy to implement and provides additional benefits, such as:
- a compromise to the infrastructure of one proxy node should not easily affect another
- the damage of compromising one proxy node's signing key is limited to a single requesting connector node
- signing key and metadata rotations can be performed on an individual connector node basis, removing the need to coordinate with multiple requesting MS

## Consequences

- Having to maintain multiple entries in Terraform
- More complex deployment rollouts
