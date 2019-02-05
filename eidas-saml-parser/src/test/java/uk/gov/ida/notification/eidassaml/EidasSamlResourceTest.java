package uk.gov.ida.notification.eidassaml;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import static org.junit.Assert.assertEquals;

public class EidasSamlResourceTest {
    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
        .addResource(new EidasSamlResource())
        .build();

    @Test
    public void shouldReturnRequestIdAndIssuer() {
        RequestDto request = new RequestDto();
        request.authnRequest = "pantoufles";

        ResponseDto response = resources.target("/eidasAuthnRequest")
            .request(MediaType.APPLICATION_JSON_TYPE)
            .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE))
            .readEntity(ResponseDto.class);

        assertEquals(response.requestId, "request_id");
        assertEquals(response.requestId, "request_id");
    }
}