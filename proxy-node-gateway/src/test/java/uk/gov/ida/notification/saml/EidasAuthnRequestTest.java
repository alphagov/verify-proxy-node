package uk.gov.ida.notification.saml;

import org.junit.Test;
import org.opensaml.saml.saml2.core.AuthnRequest;
import se.litsec.eidas.opensaml.common.EidasConstants;
import uk.gov.ida.notification.SamlInitializedTest;
import uk.gov.ida.notification.helpers.FileHelpers;

import static org.junit.Assert.assertEquals;

public class EidasAuthnRequestTest extends SamlInitializedTest {
    @Test
    public void shouldMapEidasRequestFromString() throws Throwable {
        String request = FileHelpers.readFileAsString("eidas_authn_request.xml");
        SamlParser parser = new SamlParser();
        AuthnRequest authnRequest = parser.parseSamlString(request);

        EidasAuthnRequest eidasAuthnRequest = EidasAuthnRequest.buildFromAuthnRequest(authnRequest);

        assertEquals(EidasConstants.EIDAS_LOA_SUBSTANTIAL, eidasAuthnRequest.getRequestedLoa());
        assertEquals("_171ccc6b39b1e8f6e762c2e4ee4ded3a", eidasAuthnRequest.getRequestId());
    }
}
