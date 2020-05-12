package uk.gov.ida.notification.translator.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;
import uk.gov.ida.eidas.logging.EidasAuthnResponseAttributesHashLogger;
import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponse;
import uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponseBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class EidasResponseAttributesHashLoggerParameterizedTest {
    private static final String REQUEST_ID = "request-id";
    private static final String DESTINATION_URL = "http://connnector.eu";

    @Mock
    private Appender<ILoggingEvent> appender;

    @Mock
    private HubResponseTranslatorRequest hubResponseTranslatorRequest;

    private TranslatedHubResponse translatedHubResponse;
    private String hashResult;

    public EidasResponseAttributesHashLoggerParameterizedTest(TranslatedHubResponse translatedHubResponse, String hashResult) {
        this.translatedHubResponse = translatedHubResponse;
        this.hashResult = hashResult;

        MockitoAnnotations.initMocks(this);
        when(hubResponseTranslatorRequest.getRequestId()).thenReturn(REQUEST_ID);
        when(hubResponseTranslatorRequest.getDestinationUrl()).thenReturn(URI.create(DESTINATION_URL));
    }

    @Test
    public void shouldLogHashForTranslatedHubResponses() {
        Logger logger = (Logger) LoggerFactory.getLogger(EidasAuthnResponseAttributesHashLogger.class);
        logger.addAppender(appender);

        EidasAuthnResponseAttributesHashLogger.logEidasAttributesHash(
                translatedHubResponse.getAttributes().orElse(null),
                hubResponseTranslatorRequest.getRequestId(),
                hubResponseTranslatorRequest.getDestinationUrl());

        ArgumentCaptor<ILoggingEvent> loggingEventArgumentCaptor = ArgumentCaptor.forClass(ILoggingEvent.class);
        verify(appender).doAppend(loggingEventArgumentCaptor.capture());

        ILoggingEvent loggingEvent = loggingEventArgumentCaptor.getValue();
        Map<String, String> mdcPropertyMap = loggingEvent.getMDCPropertyMap();

        assertThat(
                mdcPropertyMap.get(EidasAuthnResponseAttributesHashLogger.MDC_KEY_EIDAS_USER_HASH))
                .as("EidasResponse Hash Test Failure.\n" +
                        "Method used to calculate the hash (or the test data) may have changed.\n" +
                        "Caution: Hash calculation must be identical in both the Proxy Node and the Hub.")
                .isEqualTo(hashResult);
    }

    @Parameterized.Parameters(name = "Run {index}: translatedHubResponse={0}, hashResult={1}")
    public static Collection<Object[]> getHashTestParameters() {
        return Arrays.asList(
                new Object[][]{
                        {
                                TranslatedHubResponseBuilder.buildTranslatedHubResponseIdentityVerified(),
                                "ee03bda9f119f2aa08399e1c52f6568aa6fb767d11099f114b932f0280dca35a"
                        },
                        {
                                TranslatedHubResponseBuilder.buildTranslatedHubResponseIdentityVerifiedLOA1(),
                                "ee03bda9f119f2aa08399e1c52f6568aa6fb767d11099f114b932f0280dca35a"
                        },
                        {
                                TranslatedHubResponseBuilder.buildTranslatedHubResponseIdentityVerifiedNoAttributes(),
                                "b9514b7e03bb06ec0f8dca63de590e63516267c653485fb0ccb9d432da567ecd"
                        },
                        {
                                TranslatedHubResponseBuilder.buildTranslatedHubResponseAuthenticationFailed(),
                                "b9514b7e03bb06ec0f8dca63de590e63516267c653485fb0ccb9d432da567ecd"
                        },
                        {
                                TranslatedHubResponseBuilder.buildTranslatedHubResponseFirstNameAttributeOnly(),
                                "f393c25ac8f3936ac1951af11b683c646cdb370eba6bfdaac036253160031b67"
                        },
                        {
                                TranslatedHubResponseBuilder.buildTranslatedHubResponseCancellation(),
                                "b9514b7e03bb06ec0f8dca63de590e63516267c653485fb0ccb9d432da567ecd"
                        },
                        {
                                TranslatedHubResponseBuilder.buildTranslatedHubResponseRequestError(),
                                "b9514b7e03bb06ec0f8dca63de590e63516267c653485fb0ccb9d432da567ecd"
                        },
                        {
                                TranslatedHubResponseBuilder.buildTranslatedHubResponseAllAttributesMissing(),
                                "b9514b7e03bb06ec0f8dca63de590e63516267c653485fb0ccb9d432da567ecd"
                        },
                        {
                                TranslatedHubResponseBuilder.buildTranslatedHubResponseAttributesNullPidNull(),
                                "b9514b7e03bb06ec0f8dca63de590e63516267c653485fb0ccb9d432da567ecd"
                        },
                        {
                                TranslatedHubResponseBuilder.buildTranslatedHubResponseAttributesThreeFirstNamesOnlyLastVerified(),
                                "f23018e0edc64f39e66bf82e42113f8184a56ad1e53f715a99c9c3c3ce02b177"
                        },
                        {
                                TranslatedHubResponseBuilder.buildTranslatedHubResponseAttributesMultipleValues(),
                                "82b4e032d5e6f712821574d7dd83c380360ac6389938f11b71ef160f171dfb29"
                        }}
        );
    }
}
