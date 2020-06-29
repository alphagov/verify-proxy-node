package uk.gov.ida.notification.stubconnector;

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

    static {
        try {
            InitializationService.initialize();
            VerifySamlInitializer.init();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testThatEidasAuthnRequestSetsARequestDestination() {
        final SignatureSigningParameters signingParams = mock(SignatureSigningParameters.class);
        final Endpoint destinationEndpoint = mock(Endpoint.class);

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
