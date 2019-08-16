package uk.gov.ida.notification;

import org.junit.BeforeClass;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;

public class SamlInitializedTest {
    @BeforeClass
    public static void initializeSaml() throws InitializationException {
        InitializationService.initialize();
        VerifySamlInitializer.init();
    }
}
