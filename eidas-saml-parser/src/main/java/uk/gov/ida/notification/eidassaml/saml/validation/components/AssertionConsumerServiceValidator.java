package uk.gov.ida.notification.eidassaml.saml.validation.components;

import com.google.common.base.Strings;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.exceptions.authnrequest.InvalidAuthnRequestException;
import uk.gov.ida.notification.shared.proxy.MetatronProxy;

import java.net.URI;

public class AssertionConsumerServiceValidator {

    private MetatronProxy metatronProxy;

    public AssertionConsumerServiceValidator(MetatronProxy metatronProxy) {
        this.metatronProxy = metatronProxy;
    }

    public void validate(AuthnRequest request) {
        String assertionConsumerServiceURL = request.getAssertionConsumerServiceURL();
        if (Strings.isNullOrEmpty(assertionConsumerServiceURL)) {
            throw new InvalidAuthnRequestException("No Assertion Consumer Service URL supplied.");
        }
        URI assertionConsumerServiceURLFromMetatron =
                metatronProxy.getCountryMetadata(request.getIssuer().getValue()).getDestination();

        if (assertionConsumerServiceURL.equals(assertionConsumerServiceURLFromMetatron.toString())) {
            return;
        }

        throw new InvalidAuthnRequestException("Supplied AssertionConsumerServiceURL has no match in metadata. " +
                String.format("Supplied: %s. In metadata: %s.", assertionConsumerServiceURL, assertionConsumerServiceURLFromMetatron.toString()));
    }
}
