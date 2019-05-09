package uk.gov.ida.notification.exceptions;

import org.slf4j.MDC;
import uk.gov.ida.notification.shared.ProxyNodeMDCKey;

import javax.ws.rs.WebApplicationException;

public class TranslatorResponseException extends WebApplicationException {

    public TranslatorResponseException(Throwable cause, String sessionId, String hubRequestId, String eidasRequestId) {
        super(cause);
        MDC.put(ProxyNodeMDCKey.SESSION_ID.name(), sessionId);
        MDC.put(ProxyNodeMDCKey.HUB_REQUEST_ID.name(), hubRequestId);
        MDC.put(ProxyNodeMDCKey.EIDAS_REQUEST_ID.name(), eidasRequestId);
    }
}
