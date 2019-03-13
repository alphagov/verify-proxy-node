package uk.gov.ida.notification.configuration;

public class CredentialConfigurationException extends Throwable {
    public CredentialConfigurationException(Exception e) {
        super("Failed to load CredentialConfiguration", e);
    }
}
