package uk.gov.ida.notification.saml.translation;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Extensions;
import se.litsec.eidas.opensaml.common.EidasConstants;
import uk.gov.ida.notification.helpers.ConnectorNodeIssuer;
import uk.gov.ida.notification.helpers.ConnectorNodeSPType;
import uk.gov.ida.notification.saml.SamlParser;
import uk.gov.ida.saml.core.extensions.IdaAuthnContext;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
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

        AuthnRequest eidasAuthnRequest = buildEidasAuthnRequest(requestId);

        AuthnRequest hubAuthnRequest = translator.translate(eidasAuthnRequest);

        assertEquals(requestId, hubAuthnRequest.getID());
        assertEquals(IdaAuthnContext.LEVEL_2_AUTHN_CTX, getLoa(hubAuthnRequest));
    }

    private AuthnRequest buildEidasAuthnRequest(String requestId) {
        String loa = EidasConstants.EIDAS_LOA_SUBSTANTIAL;
        AuthnRequest authnRequest = mock(AuthnRequest.class, RETURNS_DEEP_STUBS);
        when(authnRequest.getIssuer()).thenReturn(new ConnectorNodeIssuer());
        when(authnRequest.getRequestedAuthnContext().getAuthnContextClassRefs().get(0).getAuthnContextClassRef()).thenReturn(loa);
        Extensions extentions = mock(Extensions.class);
        ArrayList<XMLObject> extentionChildren = new ArrayList<>();
        extentionChildren.add(new ConnectorNodeSPType());
        when(extentions.getOrderedChildren()).thenReturn(extentionChildren);
        when(authnRequest.getExtensions()).thenReturn(extentions);
        when(authnRequest.getID()).thenReturn(requestId);
        return authnRequest;
    }

    private static String getLoa(AuthnRequest hubAuthnRequest) {
        return hubAuthnRequest.getRequestedAuthnContext().getAuthnContextClassRefs().get(0).getAuthnContextClassRef();
    }
}
