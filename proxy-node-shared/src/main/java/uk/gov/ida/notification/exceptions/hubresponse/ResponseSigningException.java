package uk.gov.ida.notification.exceptions.hubresponse;

public class ResponseSigningException extends RuntimeException {

    public ResponseSigningException(Throwable cause) {
        super("Failed to sign SAML message", cause);
    }
}
