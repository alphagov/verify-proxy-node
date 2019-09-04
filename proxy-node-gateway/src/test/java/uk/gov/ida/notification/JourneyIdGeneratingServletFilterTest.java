package uk.gov.ida.notification;

import net.shibboleth.utilities.java.support.security.SecureRandomIdentifierGenerationStrategy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ida.notification.shared.Urls;

import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.ida.notification.shared.logging.ProxyNodeLoggingFilter.JOURNEY_ID_KEY;

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

    @Captor
    private ArgumentCaptor<Cookie> captorCookie;

    @Test
    public void setAJourneyIdInSessionAndRequest() throws Exception {
        String journeyId = UUID.randomUUID().toString();
        when(idGenerationStrategy.generateIdentifier()).thenReturn(journeyId);
        when(request.getSession()).thenReturn(session);
        when(request.getServerName()).thenReturn("proxy-node");
        filter.doFilter(request, response, chain);
        verify(idGenerationStrategy).generateIdentifier();
        verify(request).getSession();
        verify(session).setAttribute(JOURNEY_ID_KEY, journeyId);
        verify(request).setAttribute(JOURNEY_ID_KEY, journeyId);
        verify(request).getServerName();
        verify(response).addCookie(captorCookie.capture());
        verify(chain).doFilter(request, response);
        verifyNoMoreInteractions(chain, request, session, idGenerationStrategy);
        verifyZeroInteractions(response);
        Cookie cookie = captorCookie.getValue();
        assertThat(cookie.getName()).isEqualTo(JOURNEY_ID_KEY.toLowerCase());
        assertThat(cookie.getValue()).isEqualTo(journeyId);
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getSecure()).isTrue();
        assertThat(cookie.getMaxAge()).isEqualTo(TimeUnit.MINUTES.toSeconds(90));
        assertThat(cookie.getDomain()).isEqualTo("proxy-node");
        assertThat(cookie.getPath()).isEqualTo(Urls.GatewayUrls.GATEWAY_ROOT);

    }
}
