package uk.gov.ida.notification.resources;

import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ida.notification.exceptions.SessionMissingException;
import uk.gov.ida.notification.session.JSONWebTokenService;
import uk.gov.ida.notification.session.SessionCookieService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.ida.notification.session.SessionCookieService.COOKIE_X_PN_SESSION;

@RunWith(MockitoJUnitRunner.class)
public class SessionCookieServiceTest {

    private SessionCookieService helper;

    @Mock
    private JSONWebTokenService jsonWebTokenService;

    @Mock
    private HttpServletResponse response;

    @Before
    public void setUp() {
        helper = new SessionCookieService(90, "domain", jsonWebTokenService);
    }

    @Test(expected = IllegalArgumentException.class)
    public void exceptionWhenCookieExpiryIsZero() {
        new SessionCookieService(0, "", jsonWebTokenService);
    }

    @Test(expected = NullPointerException.class)
    public void exceptionWhenCookieDomainIsNull() {
        new SessionCookieService(90, null, jsonWebTokenService);
    }

    @Test
    public void cookieIsSetFromJWTClaims() {
        ArgumentCaptor<Cookie> cookieArgumentCaptor = ArgumentCaptor.forClass(Cookie.class);
        ArgumentCaptor<JWTClaimsSet> jwtClaimSetArgumentCaptor = ArgumentCaptor.forClass(JWTClaimsSet.class);
        when(jsonWebTokenService.create(any(JWTClaimsSet.class))).thenReturn("a jwt string");
        Map<String, Object> claims = Map.of("foo", 1, "bar", true);
        helper.setCookie(claims, response);
        verify(response).addCookie(cookieArgumentCaptor.capture());
        verify(jsonWebTokenService).create(jwtClaimSetArgumentCaptor.capture());
        Cookie result = cookieArgumentCaptor.getValue();
        assertThat(result.getName()).isEqualTo(COOKIE_X_PN_SESSION);
        assertThat(result.getDomain()).isEqualTo("domain");
        assertThat(result.isHttpOnly()).isTrue();
        assertThat(result.getSecure()).isTrue();
        assertThat(result.getMaxAge()).isEqualTo(TimeUnit.MINUTES.toSeconds(90));
        assertThat(result.getValue()).isEqualTo("a jwt string");
        JWTClaimsSet jwtClaimsSet = jwtClaimSetArgumentCaptor.getValue();
        assertThat(jwtClaimsSet.getClaim("foo")).isEqualTo(1);
        assertThat(jwtClaimsSet.getClaim("bar")).isEqualTo(Boolean.TRUE);
        assertThat(jwtClaimsSet.getIssuer()).isEqualTo("domain");
        assertThat(jwtClaimsSet.getAudience()).isEqualTo(List.of("domain"));
        Date expirationTime = jwtClaimsSet.getExpirationTime();
        Calendar expectedTime = Calendar.getInstance();
        expectedTime.add(Calendar.MINUTE, 89);
        assertThat(expirationTime).isAfter(expectedTime.getTime());
        expectedTime.add(Calendar.MINUTE, 2);
        assertThat(expirationTime).isBefore(expectedTime.getTime());
    }

    @Test
    public void getClaimsFromCookie() {
        Cookie cookie = mock(Cookie.class);
        when(cookie.getName()).thenReturn(COOKIE_X_PN_SESSION);
        when(cookie.getValue()).thenReturn("a jwt string");
        when(jsonWebTokenService.read("a jwt string")).thenReturn(new JWTClaimsSet.Builder().claim("foo", 1).build());
        Map<String, Object> data = helper.getData(new Cookie[]{cookie});
        assertThat(data.get("foo")).isEqualTo(1);
    }

    @Test(expected = SessionMissingException.class)
    public void exceptionWhenSessionCookieNotPresent() {
        Cookie cookie = mock(Cookie.class);
        when(cookie.getName()).thenReturn("not the session cookie");
        helper.getData(new Cookie[]{cookie});
    }
}