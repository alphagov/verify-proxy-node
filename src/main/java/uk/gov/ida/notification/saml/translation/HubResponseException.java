package uk.gov.ida.notification.saml.translation;

public class HubResponseException extends RuntimeException {
    HubResponseException() {
        super("Bad SAML Response from Hub");
    }
    HubResponseException(String message) {
        super("Bad SAML Response from Hub: " + message);
    }
}
