package uk.gov.ida.notification.translator.logging;

import org.opensaml.saml.saml2.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class HubResponseTranslatorLoggerHelper {

    private static final Logger log = LoggerFactory.getLogger(HubResponseTranslatorLoggerHelper.class);
    public static final String EIDAS_RESPONSE_LOGGER_MESSAGE = "eIDAS Response Attributes";

    public interface HubResponseTranslatorLoggerAttributes {
        String EIDAS_RESPONSE_ID = "eidasResponse.id";
        String EIDAS_RESPONSE_IN_RESPONSE_TO = "eidasResponse.inResponseTo";
        String EIDAS_RESPONSE_DESTINATION = "eidasResponse.destination";
        String EIDAS_RESPONSE_ISSUER = "eidasResponse.issuer";
    }

    public static void logEidasResponse(Response eidasResponse) {
        try {
            MDC.put(HubResponseTranslatorLoggerAttributes.EIDAS_RESPONSE_ID, eidasResponse.getID() != null ? eidasResponse.getID() : "");
            MDC.put(HubResponseTranslatorLoggerAttributes.EIDAS_RESPONSE_IN_RESPONSE_TO, eidasResponse.getInResponseTo() != null ? eidasResponse.getInResponseTo() : "");
            MDC.put(HubResponseTranslatorLoggerAttributes.EIDAS_RESPONSE_DESTINATION, eidasResponse.getDestination() != null ? eidasResponse.getDestination() : "");
            MDC.put(HubResponseTranslatorLoggerAttributes.EIDAS_RESPONSE_ISSUER, eidasResponse.getIssuer() != null ? eidasResponse.getIssuer().getValue() : "");
            log.info(EIDAS_RESPONSE_LOGGER_MESSAGE);
        } finally {
            MDC.remove(HubResponseTranslatorLoggerAttributes.EIDAS_RESPONSE_ID);
            MDC.remove(HubResponseTranslatorLoggerAttributes.EIDAS_RESPONSE_IN_RESPONSE_TO);
            MDC.remove(HubResponseTranslatorLoggerAttributes.EIDAS_RESPONSE_DESTINATION);
            MDC.remove(HubResponseTranslatorLoggerAttributes.EIDAS_RESPONSE_ISSUER);
        }
    }
}
