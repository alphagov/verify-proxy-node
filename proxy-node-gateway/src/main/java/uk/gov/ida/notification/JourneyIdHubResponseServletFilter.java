package uk.gov.ida.notification;

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

import static uk.gov.ida.notification.JourneyIdGeneratingServletFilter.COOKIE_GATEWAY_JOURNEY_ID;

public class JourneyIdHubResponseServletFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        chain.doFilter(servletRequest, servletResponse);
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Arrays.stream(cookies)
                    .filter(c -> COOKIE_GATEWAY_JOURNEY_ID.equals(c.getName()))
                    .forEach(c -> {
                        c.setMaxAge(0);
                        ((HttpServletResponse) servletResponse).addCookie(c);
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
