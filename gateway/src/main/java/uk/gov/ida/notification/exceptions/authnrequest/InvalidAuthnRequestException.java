package uk.gov.ida.notification.exceptions.authnrequest;

public class InvalidAuthnRequestException extends RuntimeException {
    public InvalidAuthnRequestException(String message) {
        super("Bad Authn Request from Connector Node: " + message);
    }
}
