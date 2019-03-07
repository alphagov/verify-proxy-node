package uk.gov.ida.notification.translator.logging;

import uk.gov.ida.notification.contracts.verifyserviceprovider.Attribute;
import uk.gov.ida.notification.contracts.verifyserviceprovider.Attributes;
import uk.gov.ida.saml.core.transformers.EidasResponseAttributesHashLogger;

public final class EidasResponseAttributesHashLoggerHelper {

    EidasResponseAttributesHashLogger eidasResponseAttributesHashLogger;

    private EidasResponseAttributesHashLoggerHelper() {
    }

    public EidasResponseAttributesHashLoggerHelper(EidasResponseAttributesHashLogger eidasResponseAttributesHashLogger) {
        this.eidasResponseAttributesHashLogger = eidasResponseAttributesHashLogger;
    }

    public EidasResponseAttributesHashLogger applyAttributesToHashLogger(Attributes attributes, String pid) {

        eidasResponseAttributesHashLogger.setPid(pid);

        if (attributes != null) {

            attributes.getFirstNames().stream()
                    .filter(Attribute::isVerified)
                    .findFirst()
                    .ifPresent(firstName -> eidasResponseAttributesHashLogger.setFirstName(firstName.getValue()));

            attributes.getMiddleNames().forEach(
                    middleName -> eidasResponseAttributesHashLogger.addMiddleName(middleName.getValue())
            );

            attributes.getSurnames().forEach(
                    surname -> eidasResponseAttributesHashLogger.addSurname(surname.getValue())
            );

            attributes.getDatesOfBirth().stream()
                    .filter(Attribute::isVerified)
                    .findFirst()
                    .ifPresent(dateOfBirth -> eidasResponseAttributesHashLogger.setDateOfBirth(dateOfBirth.getValue()));
        }

        return eidasResponseAttributesHashLogger;
    }
}
