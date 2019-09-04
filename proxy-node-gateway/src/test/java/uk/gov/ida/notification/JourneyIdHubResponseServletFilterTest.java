package uk.gov.ida.notification;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.ida.notification.shared.logging.ProxyNodeLoggingFilter.JOURNEY_ID_KEY;

@RunWith(MockitoJUnitRunner.class)
public class JourneyIdHubResponseServletFilterTest {

    @InjectMocks
    private JourneyIdHubResponseServletFilter filter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @Mock
    private Cookie cookie;

    @Test
    public void shouldGetJourneyIdFromSessionAndSetAsRequestAttribute() throws Exception {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute(JOURNEY_ID_KEY)).thenReturn("a journey id");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(cookie.getName()).thenReturn(JOURNEY_ID_KEY.toLowerCase());
        filter.doFilter(request, response, chain);
        verify(request).getSession();
        verify(request).getCookies();
        verify(session).getAttribute(JOURNEY_ID_KEY);
        verify(request).setAttribute(JOURNEY_ID_KEY, "a journey id");
        verify(chain).doFilter(request, response);
        verify(cookie).getName();
        verify(cookie).setMaxAge(0);
        verify(response).addCookie(cookie);
        verifyNoMoreInteractions(request, session);
        verifyZeroInteractions(response);
    }
}
