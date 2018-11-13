package uk.gov.ida.notification.saml.validation.components;

import java.util.concurrent.ConcurrentSkipListSet;

import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.AuthnRequest;

import uk.gov.ida.notification.exceptions.hubresponse.InvalidHubResponseException;

public class RequestIdWatcher {
    private final ConcurrentSkipListSet<String> seenAuthnRequestIds = new ConcurrentSkipListSet<>();

    public void observe(AuthnRequest authnRequest) {
        seenAuthnRequestIds.add(authnRequest.getID());
    }

    public boolean haveSeenRequestFor(Response response) {
        return seenAuthnRequestIds.contains(response.getInResponseTo());
    }

    public void validateSeenRequestFor(Response response) {
        if (!haveSeenRequestFor(response)) {
            throw new InvalidHubResponseException(
                String.format("Received a Response from Hub for an AuthnRequest we have not seen (ID: %s)", response.getInResponseTo()));
        }
    }
}