package uk.gov.ida.notification;

import org.opensaml.saml.saml2.core.AuthnRequest;

public class ProxyNodeSigner {

    public AuthnRequest sign(AuthnRequest authRequest) {
        return authRequest;
    }
}
