package uk.gov.ida.notification;

import uk.gov.ida.notification.shared.ProxyNodeMDCKey;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class JourneyIdHubResponseServletFilter implements Filter {

    private static final String JOURNEY_ID_KEY = ProxyNodeMDCKey.PROXY_NODE_JOURNEY_ID.name();

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
