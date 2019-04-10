package uk.gov.ida.notification.resources;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ida.notification.SamlFormViewBuilder;
import uk.gov.ida.notification.contracts.EidasSamlParserRequest;
import uk.gov.ida.notification.contracts.EidasSamlParserResponse;
import uk.gov.ida.notification.contracts.verifyserviceprovider.AuthnRequestResponse;
import uk.gov.ida.notification.proxy.EidasSamlParserProxy;
import uk.gov.ida.notification.session.GatewaySessionData;
import uk.gov.ida.notification.session.storage.SessionStore;
import uk.gov.ida.notification.shared.ProxyNodeLogger;
import uk.gov.ida.notification.shared.ProxyNodeMDCKey;
import uk.gov.ida.notification.shared.proxy.VerifyServiceProviderProxy;

import javax.servlet.http.HttpSession;
import java.net.URI;
import java.net.URISyntaxException;

import static java.util.logging.Level.INFO;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.ida.notification.resources.EidasAuthnRequestResource.SUBMIT_BUTTON_TEXT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.UNCHAINED_PUBLIC_CERT;


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
    private ProxyNodeLogger proxyNodeLogger;

    @Captor
    private ArgumentCaptor<ILoggingEvent> captorILoggingEvent;

    @Captor
    private ArgumentCaptor<EidasSamlParserRequest> captorEidasSamlParserRequest;

    @Captor
    private ArgumentCaptor<GatewaySessionData> captorGatewaySessionData;

    @Test
    public void testHappyPathRedirect() throws URISyntaxException {
        setupHappyPath();
        resource.handleRedirectBinding("eidas blob", "eidas relay state", session);
        verifyHappyPath();
    }

    @Test
    public void testHappyPath() throws URISyntaxException {
        setupHappyPath();
        resource.handlePostBinding("eidas blob", "eidas relay state", session);
        verifyHappyPath();
    }

    private void setupHappyPath() throws URISyntaxException {
        when(eidasSamlParserService.parse(any(EidasSamlParserRequest.class), any(String.class))).thenReturn(eidasSamlParserResponse);
        when(vspProxy.generateAuthnRequest(any(String.class))).thenReturn(vspResponse);
        when(eidasSamlParserResponse.getConnectorEncryptionPublicCertificate()).thenReturn(UNCHAINED_PUBLIC_CERT);
        when(eidasSamlParserResponse.getDestination()).thenReturn("destination");
        when(eidasSamlParserResponse.getIssuer()).thenReturn("issuer");
        when(eidasSamlParserResponse.getRequestId()).thenReturn("eidas request id");
        when(vspResponse.getRequestId()).thenReturn("hub request id");
        when(vspResponse.getSsoLocation()).thenReturn(new URI("http://hub.bub"));
        when(vspResponse.getSamlRequest()).thenReturn("hub blob");
        when(session.getId()).thenReturn("some session id");
    }

    private void verifyHappyPath() {
        final String sessionId = "some session id";

        verify(sessionStore).createOrUpdateSession(eq(sessionId), any(GatewaySessionData.class));
        verify(session).getId();
        verify(proxyNodeLogger).addContext(ProxyNodeMDCKey.eidasRequestId, "eidas request id");
        verify(proxyNodeLogger).addContext(ProxyNodeMDCKey.eidasIssuer, "issuer");
        verify(proxyNodeLogger).addContext(ProxyNodeMDCKey.eidasDestination, "destination");
        verify(proxyNodeLogger).addContext(ProxyNodeMDCKey.connectorPublicEncCertSuffix, StringUtils.right(UNCHAINED_PUBLIC_CERT, 10));
        verify(proxyNodeLogger).addContext(ProxyNodeMDCKey.hubRequestId, "hub request id");
        verify(proxyNodeLogger).addContext(ProxyNodeMDCKey.hubUrl, "http://hub.bub");
        verify(proxyNodeLogger).log(INFO, "Authn requests received from ESP and VSP");
        verify(eidasSamlParserService).parse(captorEidasSamlParserRequest.capture(), any(String.class));
        verify(vspProxy).generateAuthnRequest(any(String.class));
        verify(samlFormViewBuilder).buildRequest("http://hub.bub", "hub blob", SUBMIT_BUTTON_TEXT, "eidas relay state");
        verifyNoMoreInteractions(vspProxy, eidasSamlParserService, proxyNodeLogger, samlFormViewBuilder, session);
        verifyNoMoreInteractions(sessionStore);

        assertThat(captorEidasSamlParserRequest.getValue().getAuthnRequest()).isEqualTo("eidas blob");

    }
}
