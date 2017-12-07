package uk.gov.ida.notification;

import org.junit.Test;
import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.notification.resources.HubResponseResource;
import uk.gov.ida.notification.saml.SamlMessageType;
import uk.gov.ida.notification.saml.translation.HubResponse;
import uk.gov.ida.notification.views.SamlFormView;

import java.net.URI;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HubResponseResourceTest {

    @Test
    public void shouldReturnExpectedView() throws Throwable {
        SamlFormView expectedSamlFormView = buildExpectedView();
        String hubResponseAsString = "hub response encrypted form content";
        EidasProxyNodeConfiguration configuration = mock(EidasProxyNodeConfiguration.class);
        EidasResponseGenerator eidasResponseGenerator = mock(EidasResponseGenerator.class);
        SamlFormViewMapper viewMapper = mock(SamlFormViewMapper.class);
        HubResponseMapper hubResponseMapper = mock(HubResponseMapper.class);
        HubResponse hubResponse = mock(HubResponse.class);
        when(hubResponseMapper.map(hubResponseAsString)).thenReturn(hubResponse);
        when(configuration.getConnectorNodeUrl()).thenReturn(URI.create(expectedSamlFormView.getPostUrl()));
        HubResponseResource hubResponseResource = new HubResponseResource(configuration, eidasResponseGenerator, viewMapper, hubResponseMapper);
        Response eidasResponse = mock(Response.class);
        when(eidasResponseGenerator.generate(hubResponse)).thenReturn(eidasResponse);
        when(viewMapper.map(expectedSamlFormView.getPostUrl(), expectedSamlFormView.getSamlMessageType(), eidasResponse, expectedSamlFormView.getSubmitText())).thenReturn(expectedSamlFormView);

        SamlFormView view = (SamlFormView) hubResponseResource.hubResponse(hubResponseAsString);

        assertEquals(view.getSamlMessageType(), expectedSamlFormView.getSamlMessageType());
        assertEquals(view.getEncodedSamlMessage(), expectedSamlFormView.getEncodedSamlMessage());
        assertEquals(view.getPostUrl(), expectedSamlFormView.getPostUrl());
        assertEquals(view.getSubmitText(), expectedSamlFormView.getSubmitText());
    }

    public SamlFormView buildExpectedView(){
        String connectorNodeUrl = "https://connectornodeurl";
        String samlResponse = SamlMessageType.SAML_RESPONSE;
        String encodedSamlMessage = "encoded saml message";
        String submitText = "Post eIDAS Response SAML to Connector Node";
        return new SamlFormView(connectorNodeUrl, samlResponse, encodedSamlMessage, submitText);
    }
}
