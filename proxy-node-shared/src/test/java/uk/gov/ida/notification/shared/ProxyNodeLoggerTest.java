package uk.gov.ida.notification.shared;

import org.junit.Test;

import java.util.logging.Level;

import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ProxyNodeLoggerTest {
    private static final String LOG_MESSAGE = "stop your messing around";

    @Test
    public void shouldNotLogProxyNodeLoggerAsTheLogLocation() {
        ProxyNodeLogger mockLogger = mock(ProxyNodeLogger.class);
        doCallRealMethod().when(mockLogger).log(Level.INFO, LOG_MESSAGE);
        mockLogger.log(Level.INFO, LOG_MESSAGE);
        verify(mockLogger).addContext(eq(ProxyNodeMDCKey.LOG_LOCATION), not(contains(ProxyNodeLogger.class.getName()))
      );
    }
}