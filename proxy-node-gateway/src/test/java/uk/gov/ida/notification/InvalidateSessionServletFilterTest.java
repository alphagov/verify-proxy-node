package uk.gov.ida.notification;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InvalidateSessionServletFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Test
    public void filterChainShouldRunBeforeSessionInvalidated() throws IOException, ServletException {
        when(request.getSession(false)).thenReturn(session);
        new InvalidateSessionServletFilter().doFilter(request, response, filterChain);
        InOrder inOrder = inOrder(request, filterChain, session);
        inOrder.verify(filterChain).doFilter(request, response);
        inOrder.verify(request).getSession(false);
        inOrder.verify(session).invalidate();
        inOrder.verifyNoMoreInteractions();
        verifyNoInteractions(response);
    }
}
