package uk.gov.ida.notification.shared;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.logging.Level;

import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ProxyNodeLoggerTest {

    @Spy
    private ProxyNodeLogger proxyNodeLogger = new ProxyNodeLogger();

    @Test
    public void shouldNotLogProxyNodeLoggerAsTheLogLocation() {
        proxyNodeLogger.log(Level.INFO, "stop your messing around");
        verify(proxyNodeLogger).addContext(eq(ProxyNodeMDCKey.LOG_LOCATION), not(contains(ProxyNodeLogger.class.getName()))
        );
    }
}