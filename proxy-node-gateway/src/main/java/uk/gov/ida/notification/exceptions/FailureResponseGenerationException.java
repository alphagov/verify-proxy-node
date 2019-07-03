package uk.gov.ida.notification.exceptions;

import org.slf4j.MDC;
import uk.gov.ida.notification.shared.logging.ProxyNodeMDCKey;

public class FailureResponseGenerationException extends ErrorPageException {
    public FailureResponseGenerationException(Throwable cause) {
        this(cause, null);
    }

    public FailureResponseGenerationException(Throwable cause, String eidasRequestId) {
        super(cause);
        MDC.put(ProxyNodeMDCKey.EIDAS_REQUEST_ID.name(), eidasRequestId);
    }
}
