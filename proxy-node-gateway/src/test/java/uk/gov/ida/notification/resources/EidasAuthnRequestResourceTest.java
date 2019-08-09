package uk.gov.ida.notification.resources;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import io.dropwizard.logging.LoggingUtil;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
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
import uk.gov.ida.notification.helpers.SelfSignedCertificateGenerator;
import uk.gov.ida.notification.proxy.EidasSamlParserProxy;
import uk.gov.ida.notification.session.GatewaySessionData;
import uk.gov.ida.notification.session.SessionCookieService;
import uk.gov.ida.notification.session.storage.SessionStore;
import uk.gov.ida.notification.shared.logging.ProxyNodeLogger;
import uk.gov.ida.notification.shared.logging.ProxyNodeMDCKey;
import uk.gov.ida.notification.shared.proxy.VerifyServiceProviderProxy;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Level;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.ida.notification.helpers.ValidationTestDataUtils.sample_destinationUrl;
import static uk.gov.ida.notification.helpers.ValidationTestDataUtils.sample_eidasAuthnRequest;
import static uk.gov.ida.notification.helpers.ValidationTestDataUtils.sample_eidasRequestId;
import static uk.gov.ida.notification.helpers.ValidationTestDataUtils.sample_eidas_relay_state;
import static uk.gov.ida.notification.helpers.ValidationTestDataUtils.sample_hubSamlAuthnRequest;
import static uk.gov.ida.notification.helpers.ValidationTestDataUtils.sample_issuer;
import static uk.gov.ida.notification.helpers.ValidationTestDataUtils.sample_requestId;
import static uk.gov.ida.notification.resources.EidasAuthnRequestResource.SUBMIT_BUTTON_TEXT;


@RunWith(MockitoJUnitRunner.class)
public class EidasAuthnRequestResourceTest {

    @InjectMocks
    private EidasAuthnRequestResource resource;

    @Mock
    private SessionStore sessionStore;

    @Mock
    private EidasSamlParserProxy eidasSamlParserService;

    @Mock
    private VerifyServiceProviderProxy vspProxy;

    @Mock
    private SamlFormViewBuilder samlFormViewBuilder;

    @Mock
    private HttpSession session;

    @Mock
    private EidasSamlParserResponse eidasSamlParserResponse;

    @Mock
    private AuthnRequestResponse vspResponse;

    @Mock
    private Appender<ILoggingEvent> appender;

    @Captor
    private ArgumentCaptor<ILoggingEvent> captorILoggingEvent;

    @Captor
    private ArgumentCaptor<EidasSamlParserRequest> captorEidasSamlParserRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private SessionCookieService sessionCookieService;

    @Mock
    private EntityMetadata entityMetadata;

    @Captor
    private ArgumentCaptor<Map<String, Object>> sessionCookieClaimsCaptor;

    private Logger logger = (Logger) LoggerFactory.getLogger(ProxyNodeLogger.class);

    private String unchained_public_PEM;

    @Before
    public void init() throws Exception {
        unchained_public_PEM = new SelfSignedCertificateGenerator("happy-path-cn").getCertificateAsPEM();
    }

    @Test
    public void testHappyPathRedirect() throws URISyntaxException {
        setupHappyPath();
        resource.handleRedirectBinding(sample_eidasAuthnRequest, sample_eidas_relay_state, session, httpServletResponse);
        verifyHappyPath();
    }

    @Test
    public void testHappyPath() throws URISyntaxException {
        setupHappyPath();
        resource.handlePostBinding(sample_eidasAuthnRequest, sample_eidas_relay_state, session, httpServletResponse);
        verifyHappyPath();
    }

    private void setupHappyPath() throws URISyntaxException {
        LoggingUtil.hijackJDKLogging();
        logger.addAppender(appender);

        when(eidasSamlParserService.parse(any(EidasSamlParserRequest.class), any(String.class))).thenReturn(eidasSamlParserResponse);
        when(vspProxy.generateAuthnRequest(any(String.class))).thenReturn(vspResponse);
        when(eidasSamlParserResponse.getConnectorEncryptionPublicCertificate()).thenReturn(unchained_public_PEM);
        when(eidasSamlParserResponse.getDestination()).thenReturn(sample_destinationUrl);
        when(eidasSamlParserResponse.getIssuer()).thenReturn(sample_issuer);
        when(eidasSamlParserResponse.getRequestId()).thenReturn(sample_eidasRequestId);
        when(vspResponse.getRequestId()).thenReturn(sample_requestId);
        when(vspResponse.getSsoLocation()).thenReturn(new URI("http://hub.bub"));
        when(vspResponse.getSamlRequest()).thenReturn(sample_hubSamlAuthnRequest);
        when(session.getId()).thenReturn("some session id");
        when(session.getAttribute(ProxyNodeMDCKey.PROXY_NODE_JOURNEY_ID.name())).thenReturn("journey id");
    }

    private void verifyHappyPath() {
        final String sessionId = "some session id";

        verify(sessionStore).createOrUpdateSession(eq(sessionId), any(GatewaySessionData.class));
        verify(session).getId();
        verify(sessionCookieService).setCookie(sessionCookieClaimsCaptor.capture(), eq(httpServletResponse));
        verify(entityMetadata).setData("sample_issuer", EntityMetadata.Key.encryptionCertificate, unchained_public_PEM);
        verify(entityMetadata).setData("sample_issuer", EntityMetadata.Key.eidasDestination, sample_destinationUrl);
        Map<String, Object> claims = sessionCookieClaimsCaptor.getValue();
        assertThat(claims.size()).isEqualTo(4);
        assertThat(claims.get(GatewaySessionData.Keys.eidasRequestId.name())).isEqualTo(sample_eidasRequestId);
        assertThat(claims.get(GatewaySessionData.Keys.hubRequestId.name())).isEqualTo(sample_requestId);
        assertThat(claims.get(GatewaySessionData.Keys.eidasRelayState.name())).isEqualTo(sample_eidas_relay_state);

        verify(appender).doAppend(captorILoggingEvent.capture());
        final ILoggingEvent logEvent = captorILoggingEvent.getValue();
        final Map<String, String> mdc = logEvent.getMDCPropertyMap();

        assertThat(mdc.get(ProxyNodeMDCKey.EIDAS_REQUEST_ID.name())).isEqualTo(sample_eidasRequestId);
        assertThat(mdc.get(ProxyNodeMDCKey.EIDAS_ISSUER.name())).isEqualTo(sample_issuer);
        assertThat(mdc.get(ProxyNodeMDCKey.EIDAS_DESTINATION.name())).isEqualTo(sample_destinationUrl);
        assertThat(mdc.get(ProxyNodeMDCKey.CONNECTOR_PUBLIC_ENC_CERT_SUFFIX.name())).isEqualTo(StringUtils.right(unchained_public_PEM, 10));
        assertThat(mdc.get(ProxyNodeMDCKey.HUB_REQUEST_ID.name())).isEqualTo(sample_requestId);
        assertThat(mdc.get(ProxyNodeMDCKey.HUB_URL.name())).isEqualTo("http://hub.bub");
        assertThat(logEvent.getLevel().toString()).isEqualTo(Level.INFO.toString());
        assertThat(logEvent.getMessage()).isEqualTo("Authn requests received from ESP and VSP");

        verify(eidasSamlParserService).parse(captorEidasSamlParserRequest.capture(), any(String.class));
        verify(vspProxy).generateAuthnRequest(any(String.class));
        verify(samlFormViewBuilder).buildRequest("http://hub.bub", sample_hubSamlAuthnRequest, SUBMIT_BUTTON_TEXT, "journey id");
        verify(session).getAttribute(ProxyNodeMDCKey.PROXY_NODE_JOURNEY_ID.name());
        verifyNoMoreInteractions(vspProxy, eidasSamlParserService, appender, samlFormViewBuilder, session);
        verifyNoMoreInteractions(sessionStore);

        assertThat(captorEidasSamlParserRequest.getValue().getAuthnRequest()).isEqualTo(sample_eidasAuthnRequest);
    }
}
