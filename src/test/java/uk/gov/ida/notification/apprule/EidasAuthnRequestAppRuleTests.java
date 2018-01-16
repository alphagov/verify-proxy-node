package uk.gov.ida.notification.apprule;

import org.glassfish.jersey.internal.util.Base64;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.saml.saml2.core.AuthnRequest;
import uk.gov.ida.notification.apprule.base.ProxyNodeAppRuleTestBase;
import uk.gov.ida.notification.helpers.EidasAuthnRequestBuilder;
import uk.gov.ida.notification.helpers.HtmlHelpers;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;
import uk.gov.ida.notification.saml.SamlParser;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;

import static org.junit.Assert.assertEquals;

public class EidasAuthnRequestAppRuleTests extends ProxyNodeAppRuleTestBase {
    private SamlObjectMarshaller marshaller;
    private SamlParser parser;

    @Before
    public void setup() throws Throwable {
        marshaller = new SamlObjectMarshaller();
        parser = new SamlParser();
    }

    @Test
    public void postBindingReturnsHubAuthnRequestForm() throws Throwable {
        EidasAuthnRequestBuilder builder = new EidasAuthnRequestBuilder();
        AuthnRequest eidasAuthnRequest = builder.build();

        String encodedRequest = Base64.encodeAsString(marshaller.transformToString(eidasAuthnRequest));
        Form postForm = new Form().param(SamlFormMessageType.SAML_REQUEST, encodedRequest);

        String html = proxyNodeAppRule.target("/SAML2/SSO/POST").request()
                .post(Entity.form(postForm))
                .readEntity(String.class);

        String decodedHubAuthnRequest = HtmlHelpers.getValueFromForm(html, "saml-form", SamlFormMessageType.SAML_REQUEST);
        AuthnRequest hubAuthnRequest = parser.parseSamlString(decodedHubAuthnRequest);

        assertEquals(eidasAuthnRequest.getID(), hubAuthnRequest.getID());
    }

    @Test
    public void redirectBindingReturnsHubAuthnRequestForm() throws Throwable {
        EidasAuthnRequestBuilder builder = new EidasAuthnRequestBuilder();
        AuthnRequest eidasAuthnRequest = builder.build();
        String encodedRequest = Base64.encodeAsString(marshaller.transformToString(eidasAuthnRequest));

        String html = proxyNodeAppRule.target("/SAML2/SSO/Redirect")
                .queryParam(SamlFormMessageType.SAML_REQUEST, encodedRequest)
                .request()
                .get()
                .readEntity(String.class);

        String decodedHubAuthnRequest = HtmlHelpers.getValueFromForm(html, "saml-form", SamlFormMessageType.SAML_REQUEST);
        AuthnRequest hubAuthnRequest = parser.parseSamlString(decodedHubAuthnRequest);

        assertEquals(eidasAuthnRequest.getID(), hubAuthnRequest.getID());
    }
}
