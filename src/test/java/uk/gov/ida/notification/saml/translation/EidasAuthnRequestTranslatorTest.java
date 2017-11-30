package uk.gov.ida.notification.saml.translation;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.AuthnRequest;
import se.litsec.eidas.opensaml.common.EidasConstants;
import uk.gov.ida.saml.core.extensions.IdaAuthnContext;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EidasAuthnRequestTranslatorTest {

    @BeforeClass
    public static void setup() throws Exception {
        InitializationService.initialize();
    }

    @Test
    public void shouldBuildHubAuthnRequestWithSameId() throws Exception {
        String requestId = "any id";
        EidasAuthnRequestTranslator translator = new EidasAuthnRequestTranslator("any", "other");
        EidasAuthnRequest eidasAuthnRequest = mock(EidasAuthnRequest.class);
        when(eidasAuthnRequest.getRequestedLoa()).thenReturn(EidasConstants.EIDAS_LOA_SUBSTANTIAL);
        when(eidasAuthnRequest.getRequestId()).thenReturn(requestId);

        AuthnRequest hubAuthnRequest = translator.translate(eidasAuthnRequest);

        assertEquals(requestId, hubAuthnRequest.getID());
        assertEquals(IdaAuthnContext.LEVEL_2_AUTHN_CTX, getLoa(hubAuthnRequest));
    }


    private static String getLoa(AuthnRequest hubAuthnRequest) {
        return hubAuthnRequest.getRequestedAuthnContext().getAuthnContextClassRefs().get(0).getAuthnContextClassRef();
    }
}
