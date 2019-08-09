package uk.gov.ida.notification;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.ida.notification.session.SessionCookieService.COOKIE_X_PN_SESSION;

@RunWith(MockitoJUnitRunner.class)
public class RemoveSessionCookieFilterTest {

    private RemoveSessionCookieFilter filter;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpServletRequest request;

    @Mock
    private FilterChain chain;

    @Mock
    private Cookie cookie;

    @Before
    public void setUp() {
        filter = new RemoveSessionCookieFilter();
    }

    @Test
    public void whenNullCookieThenNoInteractionsWithResponse() throws Exception {
        when(request.getCookies()).thenReturn(null);
        filter.doFilter(request, response, chain);
        InOrder inOrder = inOrder(chain, request);
        inOrder.verify(chain).doFilter(request, response);
        inOrder.verify(request).getCookies();
        verifyNoMoreInteractions(chain, request);
        verifyZeroInteractions(response);
    }

    @Test
    public void whenNoSessionCookieThenNoInteractionsWithResponse() throws Exception {
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(cookie.getName()).thenReturn("non matching cookie");
        filter.doFilter(request, response, chain);
        InOrder inOrder = inOrder(chain, request, cookie);
        inOrder.verify(chain).doFilter(request, response);
        inOrder.verify(request).getCookies();
        inOrder.verify(cookie).getName();
        verifyNoMoreInteractions(chain, request);
        verifyZeroInteractions(response);
    }

    @Test
    public void whenSessionCookieThenSetMaxAgeToZero() throws Exception {
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(cookie.getName()).thenReturn(COOKIE_X_PN_SESSION);
        filter.doFilter(request, response, chain);
        InOrder inOrder = inOrder(chain, request, cookie, response);
        inOrder.verify(chain).doFilter(request, response);
        inOrder.verify(request).getCookies();
        inOrder.verify(cookie).getName();
        inOrder.verify(cookie).setMaxAge(0);
        inOrder.verify(response).addCookie(cookie);
        verifyNoMoreInteractions(chain, request, cookie, response);
    }
}
