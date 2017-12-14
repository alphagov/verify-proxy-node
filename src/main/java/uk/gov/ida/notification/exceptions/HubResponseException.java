package uk.gov.ida.notification.exceptions;

public class HubResponseException extends RuntimeException {
    public HubResponseException(String message) {
        super("Bad SAML Response from Hub: " + message);
    }
}
