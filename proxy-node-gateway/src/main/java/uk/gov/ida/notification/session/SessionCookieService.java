package uk.gov.ida.notification.session;

import com.nimbusds.jwt.JWTClaimsSet;
import uk.gov.ida.notification.exceptions.SessionMissingException;
import uk.gov.ida.notification.shared.Urls;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ext.Provider;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Provider
public class SessionCookieService {

    public static final String COOKIE_X_PN_SESSION = "x_pn_session";
    private final int cookieExpiryMinutes;
    private final String gatewayDomain;
    private final JSONWebTokenService jsonWebTokenService;

    public SessionCookieService(int cookieExpiryMinutes, String gatewayDomain, JSONWebTokenService jsonWebTokenService) {
        if (cookieExpiryMinutes == 0) {
            throw new IllegalArgumentException("cookieExpiryMinutes cannot be 0, this will set cookies to delete");
        }
        this.gatewayDomain = Objects.requireNonNull(gatewayDomain, "gatewayDomain is null");
        this.cookieExpiryMinutes = cookieExpiryMinutes;
        this.jsonWebTokenService = jsonWebTokenService;
    }

    public void setCookie(Map<String, Object> claims, HttpServletResponse httpServletResponse) {
        Calendar expiryTime = Calendar.getInstance();
        expiryTime.add(Calendar.MINUTE, cookieExpiryMinutes);
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                .expirationTime(expiryTime.getTime())
                .issuer(gatewayDomain)
                .audience(gatewayDomain);
        for (Map.Entry<String, Object> entry : claims.entrySet()) {
            builder.claim(entry.getKey(), entry.getValue());
        }
        JWTClaimsSet claimsSet = builder.build();
        String jweString = jsonWebTokenService.create(claimsSet);
        Cookie cookie = new Cookie(COOKIE_X_PN_SESSION, jweString);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge((int) TimeUnit.MINUTES.toSeconds(cookieExpiryMinutes));
        cookie.setPath(Urls.GatewayUrls.GATEWAY_ROOT);
        cookie.setDomain(gatewayDomain);
        cookie.setComment("comm1");
        httpServletResponse.addCookie(cookie);
    }

    public Map<String, Object> getData(Cookie[] cookies) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (COOKIE_X_PN_SESSION.equals(cookie.getName())) {
                    String jweString = cookie.getValue();
                    JWTClaimsSet claimsSet = jsonWebTokenService.read(jweString);
                    return Collections.unmodifiableMap(claimsSet.getClaims());
                }
            }
        }
        throw new SessionMissingException("Could not find '" + COOKIE_X_PN_SESSION + "' cookie");
    }
}
