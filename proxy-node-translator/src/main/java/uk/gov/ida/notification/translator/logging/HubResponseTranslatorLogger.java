package uk.gov.ida.notification.translator.logging;

import org.opensaml.saml.saml2.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.contracts.verifyserviceprovider.Attribute;
import uk.gov.ida.notification.contracts.verifyserviceprovider.Attributes;
import uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponse;
import uk.gov.ida.saml.core.transformers.EidasResponseAttributesHashLogger;

public class HubResponseTranslatorLogger {

    private static final Logger log = LoggerFactory.getLogger(HubResponseTranslatorLogger.class);
    public static final String EIDAS_RESPONSE_LOGGER_MESSAGE = "eIDAS Response Attributes";

    public interface HubResponseTranslatorLoggerAttributes {
        String EIDAS_RESPONSE_ID = "eidasResponse.id";
        String EIDAS_RESPONSE_IN_RESPONSE_TO = "eidasResponse.inResponseTo";
        String EIDAS_RESPONSE_DESTINATION = "eidasResponse.destination";
        String EIDAS_RESPONSE_ISSUER = "eidasResponse.issuer";
    }

    public static void logSamlResponse(Response samlResponse) {
        try {
            MDC.put(HubResponseTranslatorLoggerAttributes.EIDAS_RESPONSE_ID, samlResponse.getID() != null ? samlResponse.getID() : "");
            MDC.put(HubResponseTranslatorLoggerAttributes.EIDAS_RESPONSE_IN_RESPONSE_TO, samlResponse.getInResponseTo() != null ? samlResponse.getInResponseTo() : "");
            MDC.put(HubResponseTranslatorLoggerAttributes.EIDAS_RESPONSE_DESTINATION, samlResponse.getDestination() != null ? samlResponse.getDestination() : "");
            MDC.put(HubResponseTranslatorLoggerAttributes.EIDAS_RESPONSE_ISSUER, samlResponse.getIssuer() != null ? samlResponse.getIssuer().getValue() : "");
            log.info(EIDAS_RESPONSE_LOGGER_MESSAGE);
        } finally {
            MDC.remove(HubResponseTranslatorLoggerAttributes.EIDAS_RESPONSE_ID);
            MDC.remove(HubResponseTranslatorLoggerAttributes.EIDAS_RESPONSE_IN_RESPONSE_TO);
            MDC.remove(HubResponseTranslatorLoggerAttributes.EIDAS_RESPONSE_DESTINATION);
            MDC.remove(HubResponseTranslatorLoggerAttributes.EIDAS_RESPONSE_ISSUER);
        }
    }

    public static void logResponseAttributesHash(HubResponseTranslatorRequest request, TranslatedHubResponse translatedHubResponse) {
        final EidasResponseAttributesHashLogger attributesHashLogger = EidasResponseAttributesHashLogger.instance();
        applyAttributesToHashLogger(attributesHashLogger, translatedHubResponse.getAttributes(), translatedHubResponse.getPid());
        attributesHashLogger.logHashFor(request.getRequestId(), request.getDestinationUrl().toString());
    }

    private static void applyAttributesToHashLogger(EidasResponseAttributesHashLogger logger, Attributes attributes, String pid) {
        logger.setPid(pid);

        if (attributes == null) {
            return;
        }

        attributes.getFirstNames().stream()
                .filter(Attribute::isVerified)
                .findFirst()
                .ifPresent(firstName -> logger.setFirstName(firstName.getValue()));

        attributes.getMiddleNames().forEach(
                middleName -> logger.addMiddleName(middleName.getValue())
        );

        attributes.getSurnames().forEach(
                surname -> logger.addSurname(surname.getValue())
        );

        attributes.getDatesOfBirth().stream()
                .filter(Attribute::isVerified)
                .findFirst()
                .ifPresent(dateOfBirth -> logger.setDateOfBirth(dateOfBirth.getValue()));
    }
}
