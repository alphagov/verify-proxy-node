package uk.gov.ida.notification.shared;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import io.dropwizard.logging.LoggingUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import uk.gov.ida.notification.shared.logging.ProxyNodeLogger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ProxyNodeLoggerTest {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ProxyNodeLogger.class);

    @Mock
    private static Appender<ILoggingEvent> appender;

    @Captor
    private static ArgumentCaptor<ILoggingEvent> loggingEventCaptor;

    static {
        LoggingUtil.hijackJDKLogging();
    }

    @Before
    public void setUp() {
        LOGGER.addAppender(appender);
    }

    @Test
    public void shouldLogCorrectlyWithInfoLevel() {
        ProxyNodeLogger.info("test-info");

        verify(appender).doAppend(loggingEventCaptor.capture());

        final ILoggingEvent logEvent = loggingEventCaptor.getValue();
        assertThat(logEvent.getMessage()).isEqualTo("test-info");
        assertThat(logEvent.getLevel().toString()).isEqualTo(Level.INFO.toString());
    }

    @Test
    public void shouldLogCorrectlyWithWarningLevel() {
        ProxyNodeLogger.warning("test-warn");

        verify(appender).doAppend(loggingEventCaptor.capture());

        final ILoggingEvent logEvent = loggingEventCaptor.getValue();
        assertThat(logEvent.getMessage()).isEqualTo("test-warn");
        assertThat(logEvent.getLevel().toString()).isEqualTo(Level.WARN.toString());
    }

    @Test
    public void shouldLogCorrectlyWithErrorLevel() {
        ProxyNodeLogger.error("test-error");

        verify(appender).doAppend(loggingEventCaptor.capture());

        final ILoggingEvent logEvent = loggingEventCaptor.getValue();
        assertThat(logEvent.getMessage()).isEqualTo("test-error");
        assertThat(logEvent.getLevel().toString()).isEqualTo(Level.ERROR.toString());
    }
}
