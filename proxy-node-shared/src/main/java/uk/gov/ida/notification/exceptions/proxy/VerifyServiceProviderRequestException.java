package uk.gov.ida.notification.exceptions.proxy;

import org.slf4j.MDC;
import uk.gov.ida.notification.exceptions.ErrorPageException;
import uk.gov.ida.notification.shared.logging.ProxyNodeMDCKey;

public class VerifyServiceProviderRequestException extends ErrorPageException {

    public VerifyServiceProviderRequestException(Throwable cause, String sessionId) {
        super(cause);
        MDC.put(ProxyNodeMDCKey.SESSION_ID.name(), sessionId);
    }
}
