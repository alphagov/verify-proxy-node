package uk.gov.ida.notification.exceptions.hubresponse;

import uk.gov.ida.saml.core.validation.SamlTransformationErrorException;

public class InvalidHubResponseException extends RuntimeException {

    private static final String EXCEPTION_PREFIX = "Bad IDP Response from Hub: ";

    public InvalidHubResponseException(String message) {
        super(EXCEPTION_PREFIX + message);
    }

    public InvalidHubResponseException(String message, SamlTransformationErrorException exception) {
        super(EXCEPTION_PREFIX + message, exception);
    }
}
