package uk.gov.ida.notification.saml.validation.components;

import org.opensaml.saml.saml2.core.Issuer;
import uk.gov.ida.notification.exceptions.authnrequest.InvalidAuthnRequestException;

public class RequestIssuerValidator {
    public void validate(Issuer issuer) {
        if (issuer == null) throw new InvalidAuthnRequestException("Missing Issuer");
    }
}
