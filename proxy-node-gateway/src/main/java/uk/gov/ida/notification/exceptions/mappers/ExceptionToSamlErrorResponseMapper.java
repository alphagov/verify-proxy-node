package uk.gov.ida.notification.exceptions.mappers;

import io.prometheus.client.Counter;
import uk.gov.ida.notification.MetricsUtils;
import uk.gov.ida.notification.SamlFormViewBuilder;
import uk.gov.ida.notification.contracts.SamlFailureResponseGenerationRequest;
import uk.gov.ida.notification.exceptions.FailureResponseGenerationException;
import uk.gov.ida.notification.exceptions.FailureSamlResponseException;
import uk.gov.ida.notification.proxy.TranslatorProxy;
import uk.gov.ida.notification.session.GatewaySessionData;
import uk.gov.ida.notification.session.storage.SessionStore;
import uk.gov.ida.notification.shared.logging.ProxyNodeLogger;
import uk.gov.ida.notification.views.SamlFormView;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import java.net.URI;
import java.util.logging.Level;

import static java.text.MessageFormat.format;

public class ExceptionToSamlErrorResponseMapper implements ExceptionMapper<FailureSamlResponseException> {
    // TODO multi-country PN will need a way to distinguish these counters per country
    private static final Counter FAILURE_SAML_ERROR = Counter.build(
            MetricsUtils.LABEL_PREFIX + "_failure_saml_error_responses_total",
            "Number of failure eIDAS SAML responses To Verify Proxy Node")
            .register();

    private final SamlFormViewBuilder samlFormViewBuilder;
    private final TranslatorProxy translatorProxy;
    private final SessionStore sessionStorage;

    private HttpServletRequest httpServletRequest;
    private UriInfo uriInfo;

    public ExceptionToSamlErrorResponseMapper(SamlFormViewBuilder samlFormViewBuilder, TranslatorProxy translatorProxy, SessionStore sessionStorage) {
        this.samlFormViewBuilder = samlFormViewBuilder;
        this.translatorProxy = translatorProxy;
        this.sessionStorage = sessionStorage;
    }

    @Context
    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    @Context
    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public Response toResponse(FailureSamlResponseException exception) {
        ProxyNodeLogger.logException(exception, Level.WARNING,
                                     format("Error whilst contacting URI [{0}]", uriInfo.getAbsolutePath()));

        final String sessionId = httpServletRequest.getSession().getId();
        final GatewaySessionData sessionData = getSessionData(sessionId);

        final String samlErrorResponse = translatorProxy.getSamlErrorResponse(
                new SamlFailureResponseGenerationRequest(
                        exception.getResponseStatus(),
                        sessionData.getEidasRequestId(),
                        sessionData.getEidasDestination(),
                        URI.create(sessionData.getEidasIssuer())
                ));

        final SamlFormView samlFormView = samlFormViewBuilder.buildResponse(
                sessionData.getEidasDestination(),
                samlErrorResponse,
                sessionData.getEidasRelayState());

        FAILURE_SAML_ERROR.inc();
        return Response.ok().entity(samlFormView).build();
    }

    private GatewaySessionData getSessionData(String sessionId) {
        try {
            return sessionStorage.getSession(sessionId);
        } catch (Throwable e) {
            throw new FailureResponseGenerationException(e);
        }
    }
}
