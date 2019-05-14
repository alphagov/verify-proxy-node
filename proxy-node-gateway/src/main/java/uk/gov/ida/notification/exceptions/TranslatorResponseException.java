package uk.gov.ida.notification.exceptions;

import org.slf4j.MDC;
import uk.gov.ida.common.ExceptionType;
import uk.gov.ida.exceptions.ApplicationException;
import uk.gov.ida.notification.shared.ProxyNodeMDCKey;

import javax.ws.rs.core.Response;

public class TranslatorResponseException extends FailureSamlResponseException {

    public TranslatorResponseException(ApplicationException cause, String sessionId, String hubRequestId, String eidasRequestId) {
        super(cause);
        MDC.put(ProxyNodeMDCKey.SESSION_ID.name(), sessionId);
        MDC.put(ProxyNodeMDCKey.HUB_REQUEST_ID.name(), hubRequestId);
        MDC.put(ProxyNodeMDCKey.EIDAS_REQUEST_ID.name(), eidasRequestId);
    }

    @Override
    public Response.Status getResponseStatus() {
        ApplicationException cause = (ApplicationException) this.getCause();
        return cause.getExceptionType() == ExceptionType.CLIENT_ERROR ?
                Response.Status.BAD_REQUEST : Response.Status.INTERNAL_SERVER_ERROR;

    }
}
