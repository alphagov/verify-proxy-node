package uk.gov.ida.notification.translator.configuration;

public class SignerConfigurationException extends Throwable {
    public SignerConfigurationException(Exception e) {
        super("Failed to load SignerConfiguration", e);
    }
}
