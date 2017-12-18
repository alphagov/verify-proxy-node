package uk.gov.ida.notification.apprule;

import org.glassfish.jersey.internal.util.Base64;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.notification.SamlInitializedTest;
import uk.gov.ida.notification.helpers.HtmlHelpers;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;
import uk.gov.ida.notification.saml.SamlParser;
import uk.gov.ida.saml.core.test.builders.ResponseBuilder;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;

import static org.junit.Assert.assertEquals;

public class HubResponseAppRuleTests extends SamlInitializedTest {
    private SamlObjectMarshaller marshaller = new SamlObjectMarshaller();
    private SamlParser parser;

    @ClassRule
    public static EidasProxyNodeAppRule proxyNodeAppRule = new EidasProxyNodeAppRule();

    @Before
    public void setup() throws Throwable {
        parser = new SamlParser();
    }

    @Test
    public void postingHubResponseShouldReturnEidasResponseForm() throws Throwable {
        Response hubResponse = ResponseBuilder.aValidIdpResponse().build();
        String encodedResponse = Base64.encodeAsString(marshaller.transformToString(hubResponse));
        Form postForm = new Form().param(SamlFormMessageType.SAML_RESPONSE, encodedResponse);

        String html = proxyNodeAppRule.target("/SAML2/Response/POST").request()
                .post(Entity.form(postForm))
                .readEntity(String.class);

        String decodedEidasResponse = HtmlHelpers.getValueFromForm(html, "saml-form", SamlFormMessageType.SAML_RESPONSE);
        Response eidasResponse = parser.parseSamlString(decodedEidasResponse);

        assertEquals(hubResponse.getInResponseTo(), eidasResponse.getInResponseTo());
    }
}
