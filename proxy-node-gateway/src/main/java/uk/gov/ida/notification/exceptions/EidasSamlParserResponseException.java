package uk.gov.ida.notification.exceptions;

import org.slf4j.MDC;
import uk.gov.ida.notification.shared.ProxyNodeMDCKey;

import javax.ws.rs.WebApplicationException;

public class EidasSamlParserResponseException extends WebApplicationException {
    public EidasSamlParserResponseException(Throwable cause, String sessionId) {
        super(cause);
        MDC.put(ProxyNodeMDCKey.SESSION_ID.name(), sessionId);
    }
}
