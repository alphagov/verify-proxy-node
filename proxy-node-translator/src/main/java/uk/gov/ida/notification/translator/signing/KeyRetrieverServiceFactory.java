package uk.gov.ida.notification.translator.signing;

import uk.gov.ida.notification.translator.TranslatorConfiguration;

import java.util.logging.Logger;

public final class KeyRetrieverServiceFactory {

    private static final Logger LOG = Logger.getLogger(KeyRetrieverServiceFactory.class.getName());

    public interface ServiceNames {
        public final static String CONFIG = "config";
        public final static String SOFT_HSM = "softHSM";
    }

    public final static KeyRetrieverService createKeyRetrieverService(TranslatorConfiguration configuration) {

        LOG.info("Requested KeyRetrieverService: " + configuration.getKeyRetrieverServiceName());

        if (configuration.getKeyRetrieverServiceName().equals(ServiceNames.SOFT_HSM)) {
            return new SoftHSMKeyRetrieverService(configuration);
        } else {
            return new ConfigurationKeyRetrieverService(configuration);
        }

    }
}
