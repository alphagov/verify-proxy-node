package uk.gov.ida.notification.exceptions.hubresponse;

public class InvalidHubResponseException extends RuntimeException {
    public InvalidHubResponseException(String message) {
        super("Bad IDP Response from Hub: " + message);
    }
}
