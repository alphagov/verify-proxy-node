package uk.gov.ida.notification.resources;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import io.dropwizard.logging.LoggingUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import uk.gov.ida.notification.SamlFormViewBuilder;
import uk.gov.ida.notification.contracts.EidasSamlParserRequest;
import uk.gov.ida.notification.contracts.EidasSamlParserResponse;
import uk.gov.ida.notification.contracts.verifyserviceprovider.AuthnRequestResponse;
import uk.gov.ida.notification.proxy.EidasSamlParserProxy;
import uk.gov.ida.notification.session.GatewaySessionData;
import uk.gov.ida.notification.session.storage.SessionStore;
import uk.gov.ida.notification.shared.logging.ProxyNodeLogger;
import uk.gov.ida.notification.shared.logging.ProxyNodeMDCKey;
import uk.gov.ida.notification.shared.proxy.VerifyServiceProviderProxy;

import javax.servlet.http.HttpSession;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Level;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.ida.notification.helpers.ValidationTestDataUtils.SAMPLE_DESTINATION_URL;
import static uk.gov.ida.notification.helpers.ValidationTestDataUtils.SAMPLE_EIDAS_AUTHN_REQUEST;
import static uk.gov.ida.notification.helpers.ValidationTestDataUtils.SAMPLE_EIDAS_REQUEST_ID;
import static uk.gov.ida.notification.helpers.ValidationTestDataUtils.SAMPLE_HUB_SAML_AUTHN_REQUEST;
import static uk.gov.ida.notification.helpers.ValidationTestDataUtils.SAMPLE_ENTITY_ID;
import static uk.gov.ida.notification.helpers.ValidationTestDataUtils.SAMPLE_REQUEST_ID;

@RunWith(MockitoJUnitRunner.class)
public class EidasAuthnRequestResourceTest {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ProxyNodeLogger.class);

    @Mock
    private static SessionStore sessionStore;
    @Mock
    private static EidasSamlParserProxy eidasSamlParserService;
    @Mock
    private static VerifyServiceProviderProxy vspProxy;
    @Mock
    private static SamlFormViewBuilder samlFormViewBuilder;
    @Mock
    private static HttpSession session;
    @Mock
    private static EidasSamlParserResponse eidasSamlParserResponse;
    @Mock
    private static AuthnRequestResponse vspResponse;
    @Mock
    private static Appender<ILoggingEvent> appender;

    @Captor
    private static ArgumentCaptor<ILoggingEvent> captorILoggingEvent;

    @Captor
    private static ArgumentCaptor<EidasSamlParserRequest> captorEidasSamlParserRequest;

    @InjectMocks
    private EidasAuthnRequestResource resource;

    static {
        LoggingUtil.hijackJDKLogging();
    }

    @Test
    public void testHappyPathRedirect() throws URISyntaxException {
        setupHappyPath();
        resource.handleRedirectBinding(SAMPLE_EIDAS_AUTHN_REQUEST, "eidas relay state", session);
        verifyHappyPath();
    }

    @Test
    public void testHappyPath() throws URISyntaxException {
        setupHappyPath();
        resource.handlePostBinding(SAMPLE_EIDAS_AUTHN_REQUEST, "eidas relay state", session);
        verifyHappyPath();
    }

    private void setupHappyPath() throws URISyntaxException {
        LOGGER.addAppender(appender);

        when(eidasSamlParserService.parse(any(EidasSamlParserRequest.class), any(String.class))).thenReturn(eidasSamlParserResponse);
        when(vspProxy.generateAuthnRequest(any(String.class))).thenReturn(vspResponse);
        when(eidasSamlParserResponse.getAssertionConsumerServiceLocation()).thenReturn(SAMPLE_DESTINATION_URL);
        when(eidasSamlParserResponse.getIssuerEntityId()).thenReturn(SAMPLE_ENTITY_ID);
        when(eidasSamlParserResponse.getRequestId()).thenReturn(SAMPLE_EIDAS_REQUEST_ID);
        when(vspResponse.getRequestId()).thenReturn(SAMPLE_REQUEST_ID);
        when(vspResponse.getSsoLocation()).thenReturn(new URI("http://hub.bub"));
        when(vspResponse.getSamlRequest()).thenReturn(SAMPLE_HUB_SAML_AUTHN_REQUEST);
        when(session.getId()).thenReturn("some session id");
        when(session.getAttribute(ProxyNodeMDCKey.PROXY_NODE_JOURNEY_ID.name())).thenReturn("journey id");
    }

    private void verifyHappyPath() {
        final String sessionId = "some session id";

        verify(sessionStore).createOrUpdateSession(eq(sessionId), any(GatewaySessionData.class));
        verify(session).getId();

        verify(appender).doAppend(captorILoggingEvent.capture());
        final ILoggingEvent logEvent = captorILoggingEvent.getValue();
        final Map<String, String> mdc = logEvent.getMDCPropertyMap();

        assertThat(mdc.get(ProxyNodeMDCKey.EIDAS_REQUEST_ID.name())).isEqualTo(SAMPLE_EIDAS_REQUEST_ID);
        assertThat(mdc.get(ProxyNodeMDCKey.EIDAS_ISSUER.name())).isEqualTo(SAMPLE_ENTITY_ID);
        assertThat(mdc.get(ProxyNodeMDCKey.EIDAS_DESTINATION.name())).isEqualTo(SAMPLE_DESTINATION_URL);
        assertThat(mdc.get(ProxyNodeMDCKey.HUB_REQUEST_ID.name())).isEqualTo(SAMPLE_REQUEST_ID);
        assertThat(mdc.get(ProxyNodeMDCKey.HUB_URL.name())).isEqualTo("http://hub.bub");
        assertThat(logEvent.getLevel().toString()).isEqualTo(Level.INFO.toString());
        assertThat(logEvent.getMessage()).isEqualTo("Authn requests received from ESP and VSP");

        verify(eidasSamlParserService).parse(captorEidasSamlParserRequest.capture(), any(String.class));
        verify(vspProxy).generateAuthnRequest(any(String.class));
        verify(samlFormViewBuilder).buildRequest("http://hub.bub", SAMPLE_HUB_SAML_AUTHN_REQUEST, "journey id");
        verify(session).getAttribute(ProxyNodeMDCKey.PROXY_NODE_JOURNEY_ID.name());
        verifyNoMoreInteractions(vspProxy, eidasSamlParserService, appender, samlFormViewBuilder, session);
        verifyNoMoreInteractions(sessionStore);

        assertThat(captorEidasSamlParserRequest.getValue().getAuthnRequest()).isEqualTo(SAMPLE_EIDAS_AUTHN_REQUEST);
    }
}
