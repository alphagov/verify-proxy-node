package uk.gov.ida.notification.translator.logging;

import uk.gov.ida.notification.contracts.HubResponseTranslatorRequest;
import uk.gov.ida.notification.contracts.verifyserviceprovider.Attribute;
import uk.gov.ida.notification.contracts.verifyserviceprovider.Attributes;
import uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponse;
import uk.gov.ida.saml.core.transformers.EidasResponseAttributesHashLogger;

public class HubResponseAttributesHashLogger {

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

        attributes.getFirstNames().getAllAttributes().stream()
                .filter(Attribute::isVerified)
                .findFirst()
                .ifPresent(firstName -> logger.setFirstName(firstName.getValue()));

        attributes.getMiddleNames().getAllAttributes().forEach(
                middleName -> logger.addMiddleName(middleName.getValue())
        );

        attributes.getSurnames().getAllAttributes().forEach(
                surname -> logger.addSurname(surname.getValue())
        );

        attributes.getDatesOfBirth().getAllAttributes().stream()
                .filter(Attribute::isVerified)
                .findFirst()
                .ifPresent(dateOfBirth -> logger.setDateOfBirth(dateOfBirth.getValue()));
    }
}
