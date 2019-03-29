package uk.gov.ida.notification.exceptions.mappers;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ida.notification.SamlFormViewBuilder;
import uk.gov.ida.notification.contracts.SamlFailureResponseGenerationRequest;
import uk.gov.ida.notification.exceptions.FailureResponseGenerationException;
import uk.gov.ida.notification.proxy.TranslatorProxy;
import uk.gov.ida.notification.session.GatewaySessionData;
import uk.gov.ida.notification.session.storage.SessionStore;
import uk.gov.ida.notification.views.SamlFormView;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static java.text.MessageFormat.format;

public abstract class ExceptionToSamlErrorResponseMapper<TException extends Exception> implements ExceptionMapper<TException> {

    private static final Logger LOG = LoggerFactory.getLogger(ExceptionToSamlErrorResponseMapper.class);
    private static final String SUBMIT_TEXT = "Continue";

    private final SamlFormViewBuilder samlFormViewBuilder;
    private final TranslatorProxy translatorProxy;
    private final SessionStore sessionStorage;

    private HttpServletRequest httpServletRequest;
    private UriInfo uriInfo;
    private String logId;

    ExceptionToSamlErrorResponseMapper(SamlFormViewBuilder samlFormViewBuilder, TranslatorProxy translatorProxy, SessionStore sessionStorage) {
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
    public Response toResponse(TException exception) {
        logException(exception);

        final String sessionId = httpServletRequest.getSession().getId();
        final GatewaySessionData sessionData = getSessionData(sessionId);

        final String samlErrorResponse = translatorProxy.getSamlErrorResponse(
                new SamlFailureResponseGenerationRequest(
                        getResponseStatus(exception),
                        sessionData.getEidasRequestId(),
                        sessionData.getEidasDestination()
                ));

        LOG.info(format("[eIDAS Response] received SAML error response for session '{0}', hub authn request ID '{1}', eIDAS authn request ID '{2}'",
                sessionId,
                sessionData.getHubRequestId(),
                sessionData.getEidasRequestId()));

        final SamlFormView samlFormView = samlFormViewBuilder.buildResponse(
                sessionData.getEidasDestination(),
                samlErrorResponse,
                SUBMIT_TEXT,
                sessionData.getEidasRelayState());

        return Response.ok().entity(samlFormView).build();
    }

    protected abstract Response.Status getResponseStatus(TException exception);

    protected abstract String getErrorPageMessage(TException exception);

    String getLogId() {
        return logId;
    }

    protected String getAuthnRequestId(TException exception) {
        return null;
    }

    protected String getIssuerId(TException exception) {
        return null;
    }

    protected String getSessionId(TException exception) {
        return null;
    }

    protected DateTime getIssueInstant(TException exception) {
        return null;
    }

    private void logException(TException exception) {
        this.logId = String.format("%016x", ThreadLocalRandom.current().nextLong());
        final String message = exception.getMessage();
        final String cause = Optional.ofNullable(exception.getCause()).map(Throwable::getMessage).orElse(null);

        LOG.error(format("Error whilst contacting uri [{0}]; logId: {1}; requestId: {2}; sessionId: {3}, issuer: {4}; issueInstant: {5}; message: {6}, cause: {7}",
                uriInfo.getPath(), logId, getAuthnRequestId(exception), getSessionId(exception), getIssuerId(exception), getIssueInstant(exception), message, cause),
                exception
        );
    }

    private GatewaySessionData getSessionData(String sessionId) {
        try {
            return sessionStorage.getSession(sessionId);
        } catch (Throwable e) {
            throw new FailureResponseGenerationException(e);
        }
    }
}
