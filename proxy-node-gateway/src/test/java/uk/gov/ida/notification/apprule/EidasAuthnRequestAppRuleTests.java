package uk.gov.ida.notification.apprule;

import org.apache.http.HttpStatus;
import org.glassfish.jersey.internal.util.Base64;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.security.credential.Credential;
import uk.gov.ida.notification.apprule.base.ProxyNodeAppRuleTestBase;
import uk.gov.ida.notification.helpers.EidasAuthnRequestBuilder;
import uk.gov.ida.notification.helpers.HtmlHelpers;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.saml.SamlObjectSigner;
import uk.gov.ida.notification.saml.SamlParser;

import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class EidasAuthnRequestAppRuleTests extends ProxyNodeAppRuleTestBase {

    private SamlParser parser;
    private EidasAuthnRequestBuilder eidasAuthnRequestBuilder;

    @Before
    public void setup() throws Throwable {
        parser = new SamlParser();
        eidasAuthnRequestBuilder = new EidasAuthnRequestBuilder();

    }

    @Test
    public void postBindingReturnsHubAuthnRequestForm() throws Throwable {
        AuthnRequest eidasAuthnRequest = eidasAuthnRequestBuilder.withIssuer(CONNECTOR_NODE_ENTITY_ID).build();
        samlObjectSigner.sign(eidasAuthnRequest);

        String html = postEidasAuthnRequest(eidasAuthnRequest).readEntity(String.class);
        AuthnRequest hubAuthnRequest = getHubAuthnRequestFromHtml(html);

        assertEquals(eidasAuthnRequest.getID(), hubAuthnRequest.getID());
    }

    @Test
    public void postBindingValidatesAuthnRequest_noRequestId() throws Throwable {
        EidasAuthnRequestBuilder builder = new EidasAuthnRequestBuilder();
        AuthnRequest eidasAuthnRequest = builder.withIssuer(CONNECTOR_NODE_ENTITY_ID).withoutRequestId().build();
        samlObjectSigner.sign(eidasAuthnRequest);

        Response response = postEidasAuthnRequest(eidasAuthnRequest);

        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());
        assertThat(response.readEntity(String.class), containsString("Error handling authn request."));
    }

    @Test
    public void postBindingValidatesAuthnRequest_noSignature() throws Throwable {
        EidasAuthnRequestBuilder builder = new EidasAuthnRequestBuilder();
        AuthnRequest eidasAuthnRequest = builder.withIssuer(CONNECTOR_NODE_ENTITY_ID).build();

        Response response = postEidasAuthnRequest(eidasAuthnRequest);

        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());
        assertThat(response.readEntity(String.class), containsString("Error handling authn request."));
    }

    @Test
    public void redirectBindingReturnsHubAuthnRequestForm() throws Throwable {
        AuthnRequest eidasAuthnRequest = eidasAuthnRequestBuilder.withIssuer(CONNECTOR_NODE_ENTITY_ID).build();
        samlObjectSigner.sign(eidasAuthnRequest);

        String encodedRequest = Base64.encodeAsString(marshaller.transformToString(eidasAuthnRequest));

        String html = proxyNodeAppRule.target("/SAML2/SSO/Redirect")
                .queryParam(SamlFormMessageType.SAML_REQUEST, encodedRequest)
                .request()
                .get()
                .readEntity(String.class);

        AuthnRequest hubAuthnRequest = getHubAuthnRequestFromHtml(html);

        assertEquals(eidasAuthnRequest.getID(), hubAuthnRequest.getID());
    }

    @Test
    public void redirectBindingValidatesAuthnRequest_noRequestId() throws Throwable {
        EidasAuthnRequestBuilder builder = new EidasAuthnRequestBuilder();
        AuthnRequest eidasAuthnRequest = builder.withIssuer(CONNECTOR_NODE_ENTITY_ID).withoutRequestId().build();
        samlObjectSigner.sign(eidasAuthnRequest);

        String encodedRequest = Base64.encodeAsString(marshaller.transformToString(eidasAuthnRequest));

        Response response = proxyNodeAppRule.target("/SAML2/SSO/Redirect")
                .queryParam(SamlFormMessageType.SAML_REQUEST, encodedRequest)
                .request()
                .get();

        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());
        assertThat(response.readEntity(String.class), containsString("Error handling authn request."));
    }

    @Test
    public void redirectBindingValidatesAuthnRequest_noSignature() throws Throwable {
        EidasAuthnRequestBuilder builder = new EidasAuthnRequestBuilder();
        AuthnRequest eidasAuthnRequest = builder.withIssuer(CONNECTOR_NODE_ENTITY_ID).build();

        String encodedRequest = Base64.encodeAsString(marshaller.transformToString(eidasAuthnRequest));

        Response response = proxyNodeAppRule.target("/SAML2/SSO/Redirect")
                .queryParam(SamlFormMessageType.SAML_REQUEST, encodedRequest)
                .request()
                .get();

        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatus());
        assertThat(response.readEntity(String.class), containsString("Error handling authn request."));
    }

    private AuthnRequest getHubAuthnRequestFromHtml(String html) throws IOException {
        String decodedHubAuthnRequest = HtmlHelpers.getValueFromForm(html, "saml-form", SamlFormMessageType.SAML_REQUEST);
        return parser.parseSamlString(decodedHubAuthnRequest);
    }
}
