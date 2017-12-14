package uk.gov.ida.notification;

import org.glassfish.jersey.internal.util.Base64;
import org.junit.Test;
import se.litsec.eidas.opensaml.common.EidasConstants;
import uk.gov.ida.notification.helpers.FileHelpers;
import uk.gov.ida.notification.saml.SamlParser;
import uk.gov.ida.notification.saml.translation.EidasAuthnRequest;

import static org.junit.Assert.assertEquals;

public class EidasAuthnRequestMapperTest extends SamlInitializedTest {
    @Test
    public void shouldMapEidasRequestFromString() throws Throwable {
        String request = FileHelpers.readFileAsString("eidas_authn_request.xml");
        String inputRequest = Base64.encodeAsString(request);
        SamlParser parser = new SamlParser();
        EidasAuthnRequestMapper mapper = new EidasAuthnRequestMapper(parser);

        EidasAuthnRequest eidasAuthnRequest = mapper.map(inputRequest);

        assertEquals(EidasConstants.EIDAS_LOA_SUBSTANTIAL, eidasAuthnRequest.getRequestedLoa());
        assertEquals("_171ccc6b39b1e8f6e762c2e4ee4ded3a", eidasAuthnRequest.getRequestId());
    }
}
