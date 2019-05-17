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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ProxyNodeLoggerTest {

    @Mock
    private Appender<ILoggingEvent> appender;

    @Captor
    private ArgumentCaptor<ILoggingEvent> loggingEventCaptor;

    private final Logger logger = (Logger) LoggerFactory.getLogger(ProxyNodeLogger.class);

    @Before
    public void setUp() {
        LoggingUtil.hijackJDKLogging();
        logger.addAppender(appender);
    }

    @Test
    public void shouldNotLogProxyNodeLoggerAsTheLogLocation() {
        ProxyNodeLogger.info("test");

        verify(appender).doAppend(loggingEventCaptor.capture());

        final ILoggingEvent logEvent = loggingEventCaptor.getValue();
        assertThat(logEvent.getMDCPropertyMap().get(ProxyNodeMDCKey.LOG_LOCATION.name())).doesNotContain(ProxyNodeLogger.class.getName());
        assertThat(logEvent.getMDCPropertyMap().get(ProxyNodeMDCKey.LOG_LOCATION.name())).contains("junit.runners");
    }

    @Test
    public void shouldAddExceptionContext() {
        Exception cause = new Exception("cause-message");
        Exception exception = new Exception("exception-message", cause);

        ProxyNodeLogger.logException(exception, java.util.logging.Level.SEVERE, "log-message");

        verify(appender).doAppend(loggingEventCaptor.capture());

        final ILoggingEvent logEvent = loggingEventCaptor.getValue();
        assertThat(logEvent.getLevel()).isEqualTo(Level.ERROR);
        assertThat(logEvent.getMessage()).isEqualTo("log-message");

        final Map<String, String> mdc = logEvent.getMDCPropertyMap();
        assertThat(mdc.get(ProxyNodeMDCKey.EXCEPTION_CAUSE.name())).isEqualTo("cause-message");
        assertThat(mdc.get(ProxyNodeMDCKey.EXCEPTION_MESSAGE.name())).isEqualTo("exception-message");
        assertThat(mdc.get(ProxyNodeMDCKey.EXCEPTION_STACKTRACE.name())).contains("java.lang.Exception: exception-message");
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
