package uk.gov.ida.notification.exceptions.authnrequest;

import uk.gov.ida.saml.core.validation.SamlTransformationErrorException;

public class InvalidAuthnRequestException extends RuntimeException {
    private static final String EXCEPTION_PREFIX = "Bad Authn Request from Connector Node: ";

    public InvalidAuthnRequestException(String message) {
        super(EXCEPTION_PREFIX + message);
    }

    public InvalidAuthnRequestException(String message, SamlTransformationErrorException e) {
        super(EXCEPTION_PREFIX + message, e);
    }
}
