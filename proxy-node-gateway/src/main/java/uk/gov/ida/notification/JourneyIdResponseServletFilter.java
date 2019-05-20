package uk.gov.ida.notification;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static uk.gov.ida.notification.shared.ProxyNodeLoggingFilter.JOURNEY_ID_KEY;

public class JourneyIdResponseServletFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        final String journeyId = (String) request.getSession().getAttribute(JOURNEY_ID_KEY);
        servletRequest.setAttribute(JOURNEY_ID_KEY, journeyId);
        chain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }

    @Override
    public void init(FilterConfig filterConfig) {

    }
}
