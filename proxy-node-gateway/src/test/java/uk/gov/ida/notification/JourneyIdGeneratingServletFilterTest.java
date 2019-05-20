package uk.gov.ida.notification;

import net.shibboleth.utilities.java.support.security.SecureRandomIdentifierGenerationStrategy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.ida.notification.shared.ProxyNodeLoggingFilter.JOURNEY_ID_KEY;

@RunWith(MockitoJUnitRunner.class)
public class JourneyIdGeneratingServletFilterTest {

    @InjectMocks
    private JourneyIdGeneratingServletFilter filter;

    @Mock
    private SecureRandomIdentifierGenerationStrategy idGenerationStrategy;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @Test
    public void setAJourneyIdInSessionAndRequest() throws Exception {
        String journeyId = UUID.randomUUID().toString();
        when(idGenerationStrategy.generateIdentifier()).thenReturn(journeyId);
        when(request.getSession()).thenReturn(session);
        filter.doFilter(request, response, chain);
        verify(idGenerationStrategy).generateIdentifier();
        verify(request).getSession();
        verify(session).setAttribute(JOURNEY_ID_KEY, journeyId);
        verify(request).setAttribute(JOURNEY_ID_KEY, journeyId);
        verify(chain).doFilter(request, response);
        verifyNoMoreInteractions(chain, request, session, idGenerationStrategy);
        verifyZeroInteractions(response);
    }
}
