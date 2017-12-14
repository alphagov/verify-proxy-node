package uk.gov.ida.notification;

import org.junit.Before;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;

public class SamlInitializedTest {
    @Before
    public void initializeSaml() throws InitializationException {
        InitializationService.initialize();
    }
}
