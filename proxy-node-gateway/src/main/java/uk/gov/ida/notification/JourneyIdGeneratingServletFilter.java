package uk.gov.ida.notification;

import net.shibboleth.utilities.java.support.security.SecureRandomIdentifierGenerationStrategy;
import uk.gov.ida.notification.shared.ProxyNodeMDCKey;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class JourneyIdGeneratingServletFilter implements Filter {

    private static final String JOURNEY_ID_KEY = ProxyNodeMDCKey.PROXY_NODE_JOURNEY_ID.name();

    private final SecureRandomIdentifierGenerationStrategy idGenerationStrategy;

    JourneyIdGeneratingServletFilter(SecureRandomIdentifierGenerationStrategy idGenerationStrategy) {
        this.idGenerationStrategy = idGenerationStrategy;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String journeyId = idGenerationStrategy.generateIdentifier();
        request.getSession().setAttribute(JOURNEY_ID_KEY, journeyId);
        servletRequest.setAttribute(JOURNEY_ID_KEY, journeyId);
        chain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() { }

    @Override
    public void init(FilterConfig filterConfig) { }
}
