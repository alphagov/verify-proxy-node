package uk.gov.ida.notification.exceptions;

import org.slf4j.MDC;
import uk.gov.ida.notification.shared.ProxyNodeMDCKey;

import javax.ws.rs.core.Response;

public class SessionAlreadyExistsException extends FailureSamlResponseException {

    private String sessionId;

    public SessionAlreadyExistsException(String sessionId, String hubRequestId, String eidasRequestId) {
        super("Session already exists for session_id: " + sessionId);
        MDC.put(ProxyNodeMDCKey.SESSION_ID.name(), sessionId);
        MDC.put(ProxyNodeMDCKey.HUB_REQUEST_ID.name(), hubRequestId);
        MDC.put(ProxyNodeMDCKey.EIDAS_REQUEST_ID.name(), eidasRequestId);
    }

    @Override
    public Response.Status getResponseStatus() {
        return Response.Status.BAD_REQUEST;
    }
}