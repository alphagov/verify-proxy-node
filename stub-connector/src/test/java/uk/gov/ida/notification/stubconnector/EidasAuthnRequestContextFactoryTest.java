package uk.gov.ida.notification.stubconnector;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.metadata.Endpoint;
import org.opensaml.security.credential.Credential;
import se.litsec.eidas.opensaml.common.EidasLoaEnum;
import se.litsec.eidas.opensaml.ext.SPTypeEnumeration;
import uk.gov.ida.notification.VerifySamlInitializer;

import java.util.ArrayList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EidasAuthnRequestContextFactoryTest {

    private EidasAuthnRequestContextFactory factory;

    @BeforeClass
    public static void classSetup() throws Throwable {
        InitializationService.initialize();
        VerifySamlInitializer.init();
    }

    @Before
    public void setUp() {
        factory = new EidasAuthnRequestContextFactory();
    }

    @Test
    public void testThatEidasAuthnRequestSetsARequestDestination() {
        Credential signingCredential = mock(Credential.class);
        Endpoint destinationEndpoint = mock(Endpoint.class);
        when(destinationEndpoint.getLocation()).thenReturn("a location");
        try {
            factory.generate(
                    destinationEndpoint,
                    "a connecter entity id",
                    SPTypeEnumeration.PUBLIC,
                    new ArrayList<String>(),
                    EidasLoaEnum.LOA_SUBSTANTIAL,
                    signingCredential);
        } catch (Exception e) {
            // expected
        }
        verify(destinationEndpoint).getLocation();
    }
}