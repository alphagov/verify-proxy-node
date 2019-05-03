# UK Proxy Node Public Key Infrastructure

The Public Key Infrastructure (PKI) between an EU Country and the UK Proxy Node is detailed in the table and diagram below.

This PKI allows:

* encryption and decryption of the eIDAS SAML request and response
* signing and signature validation of the eIDAS SAML request and response
* publishing of PKI metadata between the UK Proxy Node and an EU country requesting a UK identity

The table and diagram do not describe the [PKI between the Hub and UK Proxy Node](https://www.docs.verify.service.gov.uk/get-started/#get-started), this is handled by the `Verify Service Provider` service. Some return steps that do not involve any PKI are not indicated.

![](images/proxy_node_pki.svg)

Table:

| Step      | Description |
| :----:       |    :---     |
| a   |`Metadata Controller` provisions a non-extractable signing keypair in a `Amazon CloudHSM` and publishes the public key to `Metadata`. The published metadata is signed with a second non-extractable key whose public certificate can be shared with the EU country for the purpose of verifying metadata signatures.
| b   |`Metadata Controller` distributes credentials to make use of the private signing key to the `translator`
| c   | `eIDAS SAML Parser` stores a copy of the EU country's public encryption certificate from the EU country's metadata.
| 1   | The EU Country produces a signed eIDAS SAML Request, and sends it to `Proxy Node Gateway` via the browser.
| 2   | `Proxy Node Gateway` sends the signed eIDAS SAML Request to `eIDAS SAML Parser` for validation and parsing.
| 3   | `eIDAS SAML Parser` validates the signature of the SAML request using the public signing certificate obtained from EU country metadata.
| 4   | `Proxy Node Gateway` calls `Verify Service Provider` to produce a signed SAML Request for Hub.
| 5   | `Proxy Node Gateway` builds SAML Request for `Hub`, and posts to `Hub` via browser.
| 6   | `Proxy Node Gateway` receives response from `Hub`, and sends to `Translator` to process. The country public encryption certificate is sent in this request.
| 7   | `Translator` calls `Verify Service Provider` to decrypt and validate the signature of the `Hub` response, and provide decrypted user attributes.
| 8   | `Translator` builds an eiDAS SAML Response, and encrypts the user attribute assertions using the encryption key provided in the EU Country's metadata.
| 9   | `Translator` requests signature of the eiDAS SAML Response using `Amazon CloudHSM` and the private key reference obtained in step b.
| 10   | `Proxy Node Gateway` sends the eIDAS SAML response to the country via the browser.
| 11   | The EU Country validates the signature of the SAML Respose using the public signing key published in `Metadata`.
| 12   | The EU Country decrypts the user attribute assertions in the SAML Response using its private encryption key.