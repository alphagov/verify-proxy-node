package uk.gov.ida.notification.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class TranslatorSigningLoggerHelper {

    private static final Logger log = LoggerFactory.getLogger(TranslatorSigningLoggerHelper.class);

    public static void logSigningRequest(String responseId, String signingProvider) {
        try {
            MDC.put("eidasResponseID", responseId);
            MDC.put("signingProvider", signingProvider);
            log.info("Signing eIDAS response");
        } finally {
            MDC.remove("eidasResponseID");
            MDC.remove("signingProvider");
        }
    }
}
