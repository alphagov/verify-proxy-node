package uk.gov.ida.notification.apprule;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.ida.common.ExceptionType;
import uk.gov.ida.exceptions.ApplicationException;
import uk.gov.ida.notification.SamlFormViewBuilder;
import uk.gov.ida.notification.apprule.rules.TestExceptionMapperResource;
import uk.gov.ida.notification.exceptions.mappers.ErrorPageExceptionMapper;
import uk.gov.ida.notification.exceptions.mappers.ExceptionToSamlErrorResponseMapper;
import uk.gov.ida.notification.proxy.TranslatorProxy;
import uk.gov.ida.notification.session.GatewaySessionData;
import uk.gov.ida.notification.session.storage.SessionStore;
import uk.gov.ida.notification.views.SamlFormView;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SamlResponseExceptionMapperResourceRuleTest {

    private SamlFormViewBuilder samlFormViewBuilder = mock(SamlFormViewBuilder.class);
    private TranslatorProxy translatorProxy = mock(TranslatorProxy.class);
    private SessionStore sessionStore = mock(SessionStore.class);
    private UriInfo uriInfo = mock(UriInfo.class);
    private ErrorPageExceptionMapper errorPageExceptionMapper = new ErrorPageExceptionMapper(URI.create("/HubErrorPage"));

    @Rule
    public ResourceTestRule resources = ResourceTestRule.builder()
            .setTestContainerFactory(new GrizzlyWebTestContainerFactory())
            .addProvider(new ExceptionToSamlErrorResponseMapper(samlFormViewBuilder, translatorProxy, sessionStore, errorPageExceptionMapper))
            .addResource(new TestExceptionMapperResource())
            .build();

    @Before
    public void before() {
        when(sessionStore.getSession(any(String.class))).thenReturn(new GatewaySessionData("HubRequestId","EidasRequestId","EidasDestination","EidasConnectorPublicKey","EidasRelayState"));
        String nullString = null;
        when(samlFormViewBuilder.buildResponse("EidasDestination", nullString, "EidasRelayState"))
                .thenReturn(
                new SamlFormView("postUrl",
                                 "samlMessageType",
                                 "encodedSamlMessage",
                                 "relayState"));
    }

    @Test
    public void shouldMapSessionAlreadyExistsExceptionToExceptionToSamlErrorResponseMapper() {
        Response response = resources.getJerseyTest()
                .target("SessionAlreadyExistsException")
                .request()
                .get();
        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.OK);
        assertThat(checkResponseEntityIsSamlFormResponse(response.readEntity(String.class))).isTrue();
    }

    @Test
    public void shouldMapSessionAttributeExceptionToExceptionToSamlErrorResponseMapper() {
        Response response = resources.getJerseyTest()
                .target("/SessionAttributeException")
                .request()
                .get();
        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.OK);
        assertThat(checkResponseEntityIsSamlFormResponse(response.readEntity(String.class))).isTrue();
    }

    @Test
    public void shouldMapTranslatorResponseExceptionToExceptionToSamlErrorResponseMapper() {
        Response response = resources.getJerseyTest()
                .target("/TranslatorResponseException")
                .request()
                .get();
        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.OK);
        assertThat(checkResponseEntityIsSamlFormResponse(response.readEntity(String.class))).isTrue();
    }

    @Test
    public void shouldRedirectToHubErrorPageWhenExceptionOnCreateSAMLErrorResponse() {
        errorPageExceptionMapper.setUriInfo(uriInfo);
        ApplicationException applicationException = ApplicationException.createUnauditedException(
                ExceptionType.REMOTE_SERVER_ERROR,
                UUID.randomUUID());
        when(translatorProxy.getSamlErrorResponse(any())).thenThrow(applicationException);
        Response response = resources.getJerseyTest()
                .target("/TranslatorResponseException")
                .request()
                .get();
        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.OK);
        assertThat(response.readEntity(String.class)).isEqualTo(TestExceptionMapperResource.HUB_ERROR_PAGE_CONTENT);
    }

    private boolean checkResponseEntityIsSamlFormResponse(String responseEntity) {
        // The actual application returns a web page with a form but this stripped down version, returns some JSON
        // As long as it matches, we can be confident that the correct exception mapper has been used
        Pattern pattern = Pattern.compile("\\{\"postUrl\":\"postUrl\",\"samlMessageType\":\"samlMessageType\",\"encodedSamlMessage\":\"encodedSamlMessage\",\"relayState\":.*\"}");
        Matcher matcher = pattern.matcher(responseEntity);
        return matcher.matches();
    }
}
