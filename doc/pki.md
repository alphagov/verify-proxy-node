# UK Proxy Node PKI

The eIDAS Proxy Node Public Key Infrastructure (PKI) is detailed in the diagram and key below.
This PKI allows:

* encryption and decryption of the eIDAS SAML request and response
* signing and signature validation of the eIDAS SAML request and response
* publishing of PKI metadata between the UK Proxy Node and an EU connecting country

The diagram and key describe a sequential flow of of an eIDAS verify journey.

![](images/proxy_node_pki.svg)

Key:

| Step      | Description |
| :----:       |    :---     |
| a   | At startup, `metadata-controller` connects to `CloudHSM` to produce a signing keypair. It publishes the public key on `Metadata`
| b   | At startup, `translator` stores a copy of the private signing key generated in the step above
| c   | At startup, `eidas-saml-parser` reads and stores a copy of the EU country's public encryption certificate from EU country metadata
| 1   | The EU Country produces a signed eIDAS SAML Request, and sends to `gateway` via the browser
| 2   | `gateway` sends the signed eIDAS SAML Request to `eidas-saml-parser`
| 3   | `eidas-saml-parser` validates the signature of the SAML request using the public signing certificate obtained from EU country metadata. This metadata may be cached.
| 4   | `gateway` calls `verify-service-provider` to produce a signed SAML Request for Hub
| 5   | `gateway` builds SAML Request for Hub, and posts to `Hub` via browser
| 6   | `gateway` receives response from Hub, and sends to `translator` to process
| 7   | `translator` calls `verify-service-provider` to provide user attributes
| 8   | `translator` builds an eiDAS SAML Response, and signs it using the private key (from step b)
| 9   | `translator` encrypts the response for EU country using public encryption cert in step c. This cert is transmitted in step 6.
| 10   | `gateway` sends the eIDAS SAML response to the country via the browser
| 11   | The EU Country decrypts response using private encryption key
| 12   | EU Country decrypts validates Proxy Node signature using public signing key in Proxy Node metadata

The PKI between `verify-service-provider` and `Hub` is [documented here](https://www.docs.verify.service.gov.uk/get-started/#get-started).