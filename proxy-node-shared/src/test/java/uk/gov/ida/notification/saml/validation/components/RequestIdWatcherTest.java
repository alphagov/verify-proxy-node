package uk.gov.ida.notification.saml.validation.components;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.xmlsec.signature.support.SignatureException;
import uk.gov.ida.saml.core.test.builders.ResponseBuilder;

import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.notification.helpers.HubResponseBuilder.aHubResponse;
import static uk.gov.ida.saml.core.test.builders.AuthnRequestBuilder.anAuthnRequest;

public class RequestIdWatcherTest {

    private static final RequestIdWatcher REQUEST_ID_WATCHER = new RequestIdWatcher();

    @BeforeClass
    public static void setUp() throws InitializationException {
        InitializationService.initialize();
    }

    @Test
    public void shouldNotValidateIdThatHasNotBeenObserved() throws MarshallingException, SignatureException {
        Response response = aHubResponse().withInResponseTo(ResponseBuilder.DEFAULT_REQUEST_ID).build();

        assertThat(REQUEST_ID_WATCHER.haveSeenRequestFor(response)).isFalse();
    }

    @Test
    public void shouldValidateIdThatHasBeenObserved() throws MarshallingException, SignatureException {
        AuthnRequest authnRequest = anAuthnRequest().build();
        Response response = aHubResponse().withInResponseTo(authnRequest.getID()).build();

        REQUEST_ID_WATCHER.observe(authnRequest);
        assertThat(REQUEST_ID_WATCHER.haveSeenRequestFor(response)).isTrue();
    }

    @Test
    public void shouldValidateManyIdsThatHaveBeenObserved() throws MarshallingException, SignatureException {
        for (AuthnRequest authnRequest : Stream.generate(() -> anAuthnRequest().build()).limit(128).collect(toList())) {
            REQUEST_ID_WATCHER.observe(authnRequest);
            Response response = aHubResponse().withInResponseTo(authnRequest.getID()).build();

            assertThat(REQUEST_ID_WATCHER.haveSeenRequestFor(response)).isTrue();
        }
    }
}
