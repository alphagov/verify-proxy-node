package uk.gov.ida.notification;

import org.glassfish.jersey.internal.util.Base64;
import org.junit.Test;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Extensions;
import se.litsec.eidas.opensaml.common.EidasConstants;
import uk.gov.ida.notification.helpers.ConnectorNodeIssuer;
import uk.gov.ida.notification.helpers.ConnectorNodeSPType;
import uk.gov.ida.notification.saml.SamlParser;
import uk.gov.ida.notification.saml.translation.EidasAuthnRequest;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EidasAuthnRequestMapperTest {

    @Test
    public void shouldMapEidasRequestFromString(){
        String request = "request as string";
        String inputRequest = Base64.encodeAsString(request);
        String requestId = "requestId";
        SamlParser parser = mock(SamlParser.class);
        String loa = EidasConstants.EIDAS_LOA_SUBSTANTIAL;
        AuthnRequest authnRequest = buildAuthnRequest(requestId, loa);
        when(parser.parseSamlString(request, AuthnRequest.class)).thenReturn(authnRequest);
        EidasAuthnRequestMapper mapper = new EidasAuthnRequestMapper(parser);

        EidasAuthnRequest eidasAuthnRequest = mapper.map(inputRequest);

        assertEquals(eidasAuthnRequest.getClass(), EidasAuthnRequest.class);
        assertEquals(eidasAuthnRequest.getRequestedLoa(), loa);
        assertEquals(eidasAuthnRequest.getRequestId(), requestId);
    }

    private AuthnRequest buildAuthnRequest(String requestId, String loa) {
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
}
