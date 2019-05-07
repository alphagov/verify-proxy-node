package uk.gov.ida.notification.exceptions;

import org.slf4j.MDC;
import uk.gov.ida.notification.shared.ProxyNodeMDCKey;

import javax.ws.rs.WebApplicationException;

public class FailureResponseGenerationException extends WebApplicationException {
    public FailureResponseGenerationException(Throwable cause) {
        this(cause, null);
    }

    public FailureResponseGenerationException(Throwable cause, String requestId) {
        super(cause);
        MDC.put(ProxyNodeMDCKey.REQUEST_ID.name(), requestId);
    }
}
