package uk.gov.ida.notification.stubconnector;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.metadata.Endpoint;
import org.opensaml.xmlsec.SignatureSigningParameters;
import se.litsec.eidas.opensaml.common.EidasLoaEnum;
import se.litsec.eidas.opensaml.ext.SPTypeEnumeration;
import uk.gov.ida.notification.VerifySamlInitializer;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EidasAuthnRequestContextFactoryTest {

    private static final EidasAuthnRequestContextFactory factory = new EidasAuthnRequestContextFactory();

    @BeforeClass
    public static void classSetup() throws Throwable {
        InitializationService.initialize();
        VerifySamlInitializer.init();
    }

    @Test
    public void testThatEidasAuthnRequestSetsARequestDestination() {
        SignatureSigningParameters signingParams = mock(SignatureSigningParameters.class);
        Endpoint destinationEndpoint = mock(Endpoint.class);
        when(destinationEndpoint.getLocation()).thenReturn("a location");
        try {
            factory.generate(
                    destinationEndpoint,
                    "connector-entity-id",
                    SPTypeEnumeration.PUBLIC,
                    Collections.emptyList(),
                    EidasLoaEnum.LOA_SUBSTANTIAL,
                    signingParams,
                    false);
        } catch (Exception e) {
            // expected
        }
        verify(destinationEndpoint).getLocation();
    }
}
