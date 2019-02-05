package uk.gov.ida.notification.eidassaml;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.glassfish.jersey.internal.util.Base64;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import se.litsec.opensaml.utils.ObjectUtils;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import static org.junit.Assert.assertEquals;

public class EidasSamlResourceTest {
    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
        .addResource(new EidasSamlResource())
        .build();

    @Before
    public void setup() throws Exception {
        InitializationService.initialize();
    }

    @Test
    public void shouldReturnRequestIdAndIssuer() throws Exception {
        AuthnRequest authnRequest = ObjectUtils.createSamlObject(AuthnRequest.class);
        Issuer issuer = ObjectUtils.createSamlObject(Issuer.class);
        issuer.setValue("issuer");
        authnRequest.setID("request_id");
        authnRequest.setIssuer(issuer);

        RequestDto request = new RequestDto();
        request.authnRequest = Base64.encodeAsString(ObjectUtils.toString(authnRequest));

        ResponseDto response = resources.target("/eidasAuthnRequest")
            .request(MediaType.APPLICATION_JSON_TYPE)
            .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE))
            .readEntity(ResponseDto.class);

        assertEquals(response.requestId, "request_id");
        assertEquals(response.issuer, "issuer");
    }
}