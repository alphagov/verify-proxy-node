package uk.gov.ida.notification.resources;

import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ida.notification.services.EidasSamlParserService;
import uk.gov.ida.notification.SamlFormViewBuilder;
import uk.gov.ida.notification.VSPService;
import uk.gov.ida.notification.dto.EidasSamlParserRequest;
import uk.gov.ida.notification.dto.EidasSamlParserResponse;
import uk.gov.ida.notification.dto.VSPAuthnRequestResponse;

import javax.servlet.http.HttpSession;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.ida.notification.resources.EidasAuthnRequestResource.SESSION_KEY_EIDAS_CONNECTOR_PUBLIC_KEY;
import static uk.gov.ida.notification.resources.EidasAuthnRequestResource.SESSION_KEY_EIDAS_DESTINATION;
import static uk.gov.ida.notification.resources.EidasAuthnRequestResource.SESSION_KEY_EIDAS_REQUEST_ID;
import static uk.gov.ida.notification.resources.EidasAuthnRequestResource.SESSION_KEY_HUB_REQUEST_ID;
import static uk.gov.ida.notification.resources.EidasAuthnRequestResource.SUBMIT_BUTTON_TEXT;

@RunWith(MockitoJUnitRunner.class)
public class EidasAuthnRequestResourceTest {

    @InjectMocks
    private EidasAuthnRequestResource resource;

    @Mock
    private EidasSamlParserService eidasSamlParserService;

    @Mock
    private VSPService vspService;

    @Mock
    private SamlFormViewBuilder samlFormViewBuilder;

    @Mock
    private HttpSession session;

    @Mock
    private EidasSamlParserResponse eidasSamlParserResponse;

    @Mock
    private VSPAuthnRequestResponse vspResponse;

    @Mock
    private Handler logHandler;

    @Captor
    private ArgumentCaptor<LogRecord> captorLoggingEvent;

    @Captor
    private ArgumentCaptor<EidasSamlParserRequest> captorEidasSamlParserRequest;

    @Before
    public void setup() {
        Logger logger = Logger.getLogger(EidasAuthnRequestResource.class.getName());
        logger.addHandler(logHandler);
    }

    @After
    public void teardown() {
        Logger logger = Logger.getLogger(EidasAuthnRequestResource.class.getName());
        logger.removeHandler(logHandler);
    }

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
        when(eidasSamlParserService.parse(any(EidasSamlParserRequest.class))).thenReturn(eidasSamlParserResponse);
        when(vspService.generateAuthnRequest()).thenReturn(vspResponse);
        when(eidasSamlParserResponse.getConnectorPublicEncryptionKey()).thenReturn("abcdefghijk");
        when(eidasSamlParserResponse.getDestination()).thenReturn("destination");
        when(eidasSamlParserResponse.getIssuer()).thenReturn("issuer");
        when(eidasSamlParserResponse.getRequestId()).thenReturn("eidas request id");
        when(vspResponse.getRequestId()).thenReturn("hub request id");
        when(vspResponse.getSsoLocation()).thenReturn(new URI("http://hub.bub"));
        when(vspResponse.getSamlRequest()).thenReturn("hub blob");
        when(session.getId()).thenReturn("some session id");
    }

    private void verifyHappyPath() {
        verify(session).setAttribute(SESSION_KEY_EIDAS_REQUEST_ID, "eidas request id");
        verify(session).setAttribute(SESSION_KEY_EIDAS_CONNECTOR_PUBLIC_KEY, "abcdefghijk");
        verify(session).setAttribute(SESSION_KEY_EIDAS_DESTINATION, "destination");
        verify(session).setAttribute(SESSION_KEY_HUB_REQUEST_ID, "hub request id");
        verify(session).getId();
        verify(logHandler, times(7)).publish(captorLoggingEvent.capture());
        verify(eidasSamlParserService).parse(captorEidasSamlParserRequest.capture());
        verify(vspService).generateAuthnRequest();
        verify(samlFormViewBuilder).buildRequest("http://hub.bub", "hub blob", SUBMIT_BUTTON_TEXT, "eidas relay state");
        verifyNoMoreInteractions(vspService, eidasSamlParserService, logHandler, samlFormViewBuilder, session);

        assertThat(captorEidasSamlParserRequest.getValue().getAuthnRequest()).isEqualTo("eidas blob");
        List<LogRecord> allLogRecords = captorLoggingEvent.getAllValues();

        List<String> expectedLogOutput = Lists.newArrayList(
                "some session id",
                "eidas request id",
                "issuer",
                "destination",
                "bcdefghijk",
                "hub request id",
                "http://hub.bub");

        assertThat(allLogRecords.size()).isEqualTo(expectedLogOutput.size());

        for (int i = 0; i < allLogRecords.size(); i++) {
            String actualMessage = allLogRecords.get(i).getMessage();
            assertThat(actualMessage).contains(expectedLogOutput.get(i));
        }
    }
}