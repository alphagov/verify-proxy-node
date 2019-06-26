package uk.gov.ida.notification.saml.validation.components;

import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;

import java.util.concurrent.ConcurrentSkipListSet;

public class RequestIdWatcher {
    private final ConcurrentSkipListSet<String> seenAuthnRequestIds = new ConcurrentSkipListSet<>();

    public void observe(AuthnRequest authnRequest) {
        seenAuthnRequestIds.add(authnRequest.getID());
    }

    public boolean haveSeenRequestFor(Response response) {
        return seenAuthnRequestIds.contains(response.getInResponseTo());
    }
}