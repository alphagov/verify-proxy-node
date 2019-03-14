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
import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponse;
import uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponseBuilder;
import uk.gov.ida.saml.core.transformers.EidasResponseAttributesHashLogger;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class EidasResponseAttributesHashLoggerParameterizedTest {

    private final static String REQUEST_ID = "request-id";
    private final static String DESTINATION_URL = "http://connnector.eu";

    @Mock
    private Appender<ILoggingEvent> appender;

    @Mock
    HubResponseTranslatorRequest hubResponseTranslatorRequest;

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
        Logger logger = (Logger) LoggerFactory.getLogger(EidasResponseAttributesHashLogger.class);
        logger.addAppender(appender);

        HubResponseTranslatorLogger.logResponseAttributesHash(hubResponseTranslatorRequest, translatedHubResponse);

        ArgumentCaptor<ILoggingEvent> loggingEventArgumentCaptor = ArgumentCaptor.forClass(ILoggingEvent.class);
        verify(appender).doAppend(loggingEventArgumentCaptor.capture());

        ILoggingEvent loggingEvent = loggingEventArgumentCaptor.getValue();
        Map<String, String> mdcPropertyMap = loggingEvent.getMDCPropertyMap();

        assertThat(
                mdcPropertyMap.get(EidasResponseAttributesHashLogger.MDC_KEY_EIDAS_USER_HASH))
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
                                "7ea43365b70f9d94c13bcf27733e57a39f45edaa3a143e93faf15cc5f26226f3"
                        },
                        {
                                TranslatedHubResponseBuilder.buildTranslatedHubResponseIdentityVerifiedLOA1(),
                                "7ea43365b70f9d94c13bcf27733e57a39f45edaa3a143e93faf15cc5f26226f3"
                        },
                        {
                                TranslatedHubResponseBuilder.buildTranslatedHubResponseIdentityVerifiedNoAttributes(),
                                "bff88ccaf63d6700d8112b3a0409b469b6301e2fc5a1adcf1c158d600e62352f"
                        },
                        {
                                TranslatedHubResponseBuilder.buildTranslatedHubResponseAuthenticationFailed(),
                                "bff88ccaf63d6700d8112b3a0409b469b6301e2fc5a1adcf1c158d600e62352f"
                        },
                        {
                                TranslatedHubResponseBuilder.buildTranslatedHubResponseFirstNameAttributeOnly(),
                                "4d11a15b7f8a391b2f5cf4909dde7d68e34b5ac9d11762b848d3f7345bab8b0b"
                        },
                        {
                                TranslatedHubResponseBuilder.buildTranslatedHubResponseCancellation(),
                                "bff88ccaf63d6700d8112b3a0409b469b6301e2fc5a1adcf1c158d600e62352f"
                        },
                        {
                                TranslatedHubResponseBuilder.buildTranslatedHubResponseRequestError(),
                                "bff88ccaf63d6700d8112b3a0409b469b6301e2fc5a1adcf1c158d600e62352f"
                        },
                        {
                                TranslatedHubResponseBuilder.buildTranslatedHubResponseAllAttributesMissing(),
                                "bff88ccaf63d6700d8112b3a0409b469b6301e2fc5a1adcf1c158d600e62352f"
                        },
                        {
                                TranslatedHubResponseBuilder.buildTranslatedHubResponseAttributesNullPidNull(),
                                "44136fa355b3678a1146ad16f7e8649e94fb4fc21fe77e8310c060f61caaff8a"
                        },
                        {
                                TranslatedHubResponseBuilder.buildTranslatedHubResponseAttributesThreeFirstNamesOnlyLastVerified(),
                                "bc2b8e0f12328de50702ee62c7140044793cb59582bec34e73f580d55cb25e28"
                        },
                        {
                                TranslatedHubResponseBuilder.buildTranslatedHubResponseAttributesMultipleValues(),
                                "aab9e4c152098e35fb04a9a02783367fb44d0a77359a759b414a6e622ffc0571"
                        }}
        );
    }
}
