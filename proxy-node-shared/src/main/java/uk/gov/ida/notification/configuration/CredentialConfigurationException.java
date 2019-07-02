package uk.gov.ida.notification.configuration;

class CredentialConfigurationException extends Throwable {

    CredentialConfigurationException(String msg) {
        super("Failed to load CredentialConfiguration: " + msg);
    }

    CredentialConfigurationException(Exception e) {
        super("Failed to load CredentialConfiguration", e);
    }
}
