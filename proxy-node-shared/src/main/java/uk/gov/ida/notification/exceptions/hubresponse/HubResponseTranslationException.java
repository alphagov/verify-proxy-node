package uk.gov.ida.notification.exceptions.hubresponse;

public class HubResponseTranslationException extends RuntimeException {

    public HubResponseTranslationException(String message) {
        super("Bad SAML Response from Hub: " + message);
    }
}
