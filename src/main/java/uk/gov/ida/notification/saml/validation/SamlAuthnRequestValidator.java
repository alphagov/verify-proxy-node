package uk.gov.ida.notification.saml.validation;

import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.exceptions.authnrequest.InvalidAuthnRequestException;
import uk.gov.ida.notification.saml.validation.components.RequestIssuerValidator;

public class SamlAuthnRequestValidator {

    private RequestIssuerValidator requestIssuerValidator;

    public SamlAuthnRequestValidator() {
        requestIssuerValidator = new RequestIssuerValidator();
    }

    public void validateAuthnRequest(AuthnRequest request) {
        if (request == null) throw new InvalidAuthnRequestException("Null request");
        requestIssuerValidator.validate(request.getIssuer());
    }
}
