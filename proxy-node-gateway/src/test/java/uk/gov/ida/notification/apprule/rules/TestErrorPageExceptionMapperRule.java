package uk.gov.ida.notification.apprule.rules;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.glassfish.jersey.client.ClientProperties;

import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.notification.exceptions.mappers.ErrorPageExceptionMapper;
import uk.gov.ida.notification.exceptions.mappers.GenericExceptionMapper;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

public class TestErrorPageExceptionMapperRule {

    private static final String EIDAS_ERROR_PAGE_URI = "http://eidas_error_page_uri";
    private static final String GENERIC_ERROR_PAGE_URI = "http://generic_error_page_uri";

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
                                    .addProvider(new ErrorPageExceptionMapper(getUri(EIDAS_ERROR_PAGE_URI)))
                                    .addProvider(new GenericExceptionMapper(getUri(GENERIC_ERROR_PAGE_URI)))
                                    .addResource(new TestExceptionMapperResource())
                                    .build();

    @Test
    public void shouldMapEidasSamlParserResponseExceptionToErrorPageExceptionMapper() {
        Response response = getResponse("/EidasSamlParserResponseException");

        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.SEE_OTHER);
        assertThat(response.getLocation()).isEqualTo(getUri(EIDAS_ERROR_PAGE_URI));
    }

    @Test
    public void shouldMapFailureResponseGenerationExceptionToErrorPageExceptionMapper() {
        Response response = getResponse("/FailureResponseGenerationException");

        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.SEE_OTHER);
        assertThat(response.getLocation()).isEqualTo(getUri(EIDAS_ERROR_PAGE_URI));
    }

    @Test
    public void shouldMapSessionMissingExceptionToErrorPageException() {
        Response response = getResponse("/SessionMissingException");

        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.SEE_OTHER);
        assertThat(response.getLocation()).isEqualTo(getUri(EIDAS_ERROR_PAGE_URI));
    }

    @Test
    public void shouldMapVerifyServiceProviderRequestExceptionToErrorPageExceptionMapper() {
        Response response = getResponse("/VerifyServiceProviderRequestException");

        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.SEE_OTHER);
        assertThat(response.getLocation()).isEqualTo(getUri(EIDAS_ERROR_PAGE_URI));
    }

    @Test
    public void shouldMapRedisSerializationExceptionToErrorPageExceptionMapper() {
        Response response = getResponse("/RedisSerializationException");

        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.SEE_OTHER);
        assertThat(response.getLocation()).isEqualTo(getUri(EIDAS_ERROR_PAGE_URI));
    }

    @Test
    public void shouldMapNullPointerExceptionToGenericExceptionMapper() {
        Response response = getResponse("/NullPointerException");

        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.SEE_OTHER);
        assertThat(response.getLocation()).isEqualTo(getUri(GENERIC_ERROR_PAGE_URI));
    }

    private Response getResponse(String uri) {
        return resources.getJerseyTest()
                        .client().property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE)
                        .target(uri).request().get();
    }

    private static URI getUri(String errorUri) {
        URI uri =null;
        try {
            uri = new URI(errorUri);
        } catch (URISyntaxException e) {
            // this is such a bind to just get a URI
        }
        return uri;
    }

}
