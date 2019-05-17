package uk.gov.ida.notification;

import org.slf4j.MDC;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

import static uk.gov.ida.notification.shared.ProxyNodeLoggingFilter.JOURNEY_ID_KEY;

public class JourneyIdServletFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String journeyId = (String) request.getSession().getAttribute(JOURNEY_ID_KEY);
        if (journeyId == null) {
            journeyId = UUID.randomUUID().toString();
            request.getSession().setAttribute(JOURNEY_ID_KEY, journeyId);
        }
        MDC.put(JOURNEY_ID_KEY, journeyId);
        chain.doFilter(servletRequest, servletResponse);

    }

    @Override
    public void destroy() {

    }

    @Override
    public void init(FilterConfig filterConfig) {

    }
}
