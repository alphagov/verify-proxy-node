# Automate Metadata generation and signing

Date: 2019-03-18

## Status

Accepted

## Context

The [eIDAS interoperability specification][eidas-interop] requires member states (MS) that are exporting identities to host a SAML metadata file for their proxy node so that requesting member state's connector node(s) can establish trust.

The SAML metadata contains the proxy node's entity ID and the public key used to sign its SAML responses. The metadata in turn needs to be signed by a private key and the public counterpart provided to the requesting MS. The metadata is only valid for a limited period of time and every time its contents change, for example if the signing key has been rotated, the metadata needs to be regenerated and resigned.

[eidas-interop]: https://ec.europa.eu/cefdigital/wiki/display/CEFDIGITAL2018/eIDAS+Profile?preview=/80183964/80192117/eidas_interoperability_architecture_v1.00.pdf

## Decision

In our proxy node implementation, the root of trust is provided by the non-extractable private signing key generated and held within the hardware security module (HSM). Since the SAML metadata is our only way of establishing trust with a requesting MS it stands to reason that the HSM should be used to sign the metadata in addition to any SAML responses the proxy node provides. Also, since our metadata only contains the one signing key (for now) and regenerating that key would require regenerating the metadata, it makes sense to use the same key for signing the metadata and signing the SAML responses.

We will therefore automate the generation and signing of the metadata, performing it every time we spin up a new instance of the proxy node infrastructure and then at regular intervals according to the duration of metadata validity. Signing will be delegated to the HSM. The automation will be carried out via a [custom Kubernetes service][md-ctrl]. The service automatically issues an X509 certificate for the private key in the HSM which is published in the metadata and can be provided out-of-band to requesting MS (see section 6 of the [interoperability specification][eidas-interop]).

[md-ctrl]: https://github.com/alphagov/verify-metadata-controller

## Consequences

- Another application that interacts with HSM
- Duration of metadata validity needs to be determined
