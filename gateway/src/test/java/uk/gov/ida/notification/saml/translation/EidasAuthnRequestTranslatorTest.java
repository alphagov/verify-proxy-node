package uk.gov.ida.notification.saml.translation;

import org.junit.Test;
import org.opensaml.saml.saml2.core.AuthnRequest;
import se.litsec.eidas.opensaml.common.EidasConstants;
import se.litsec.eidas.opensaml.ext.SPTypeEnumeration;
import uk.gov.ida.notification.SamlInitializedTest;
import uk.gov.ida.saml.core.extensions.IdaAuthnContext;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class EidasAuthnRequestTranslatorTest extends SamlInitializedTest {
    @Test
    public void shouldBuildHubAuthnRequestWithSameId() {
        EidasAuthnRequestTranslator translator = new EidasAuthnRequestTranslator("http://proxy-node.uk", "http://hub.uk");
        EidasAuthnRequest eidasRequest = new EidasAuthnRequest(
                "request-id",
                "http://connector.eu",
                "http://proxy-node.uk",
                SPTypeEnumeration.PUBLIC,
                EidasConstants.EIDAS_LOA_SUBSTANTIAL,
                Collections.emptyList()
        );

        AuthnRequest hubAuthnRequest = translator.translate(eidasRequest);

        assertEquals("request-id", hubAuthnRequest.getID());
        assertEquals("http://proxy-node.uk", hubAuthnRequest.getIssuer().getValue());
        assertEquals("http://hub.uk", hubAuthnRequest.getDestination());
        assertEquals(IdaAuthnContext.LEVEL_2_AUTHN_CTX, getLoa(hubAuthnRequest));
    }


    private static String getLoa(AuthnRequest hubAuthnRequest) {
        return hubAuthnRequest.getRequestedAuthnContext().getAuthnContextClassRefs().get(0).getAuthnContextClassRef();
    }
}
