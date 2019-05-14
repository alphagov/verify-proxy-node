package uk.gov.ida.notification.exceptions;

import org.slf4j.MDC;
import uk.gov.ida.notification.shared.ProxyNodeMDCKey;

public class EidasSamlParserResponseException extends ErrorPageException {
    public EidasSamlParserResponseException(Throwable cause, String sessionId) {
        super(cause);
        MDC.put(ProxyNodeMDCKey.SESSION_ID.name(), sessionId);
    }
}
