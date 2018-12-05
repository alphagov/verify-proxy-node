package uk.gov.ida.notification.exceptions.authnrequest;

public class InvalidAuthnRequestException extends RuntimeException {
    private static final String EXCEPTION_PREFIX = "Bad Authn Request from Connector Node: ";

    public InvalidAuthnRequestException(String message) {
        super(EXCEPTION_PREFIX + message);
    }

    public InvalidAuthnRequestException(String message, Throwable e) {
        super(EXCEPTION_PREFIX + message, e);
    }
}
