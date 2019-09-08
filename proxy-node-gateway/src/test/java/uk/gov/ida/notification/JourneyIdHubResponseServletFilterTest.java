package uk.gov.ida.notification;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static uk.gov.ida.notification.shared.logging.ProxyNodeLoggingFilter.JOURNEY_ID_KEY;

@RunWith(MockitoJUnitRunner.class)
public class JourneyIdHubResponseServletFilterTest {

    @InjectMocks
    private JourneyIdHubResponseServletFilter filter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @Test
    public void shouldGetJourneyIdFromSessionAndSetAsRequestAttribute() throws Exception {
        filter.doFilter(request, response, chain);
        verify(request).getSession();
        verify(request).setAttribute(JOURNEY_ID_KEY, "a journey id");
        verify(chain).doFilter(request, response);
        verifyZeroInteractions(response);
    }
}
