package uk.gov.ida.notification;

import uk.gov.ida.notification.shared.logging.ProxyNodeMDCKey;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

public class JourneyIdHubResponseServletFilter implements Filter {

    private static final String JOURNEY_ID_KEY = ProxyNodeMDCKey.PROXY_NODE_JOURNEY_ID.name();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        final String journeyId = (String) request.getSession().getAttribute(JOURNEY_ID_KEY);
        servletRequest.setAttribute(JOURNEY_ID_KEY, journeyId);
        expireJourneyIdCookie(request, (HttpServletResponse) servletResponse);
        chain.doFilter(servletRequest, servletResponse);
    }

    private void expireJourneyIdCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Arrays.stream(cookies)
                    .filter(c -> JOURNEY_ID_KEY.toLowerCase().equals(c.getName()))
                    .forEach(c -> {
                        c.setMaxAge(0);
                        response.addCookie(c);
                    });
        }
    }

    @Override
    public void destroy() {

    }

    @Override
    public void init(FilterConfig filterConfig) {

    }
}
