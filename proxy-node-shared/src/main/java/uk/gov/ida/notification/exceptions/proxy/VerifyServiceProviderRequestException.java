package uk.gov.ida.notification.exceptions.proxy;

import org.slf4j.MDC;
import uk.gov.ida.notification.shared.ProxyNodeMDCKey;

import javax.ws.rs.WebApplicationException;

public class VerifyServiceProviderRequestException extends WebApplicationException {

    public VerifyServiceProviderRequestException(Throwable cause, String sessionId) {
        super(cause);
        MDC.put(ProxyNodeMDCKey.SESSION_ID.name(), sessionId);
    }
}
