package uk.gov.ida.notification.translator.saml;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;
import se.litsec.eidas.opensaml.common.EidasConstants;
import se.litsec.eidas.opensaml.ext.attributes.AttributeConstants;
import se.litsec.eidas.opensaml.ext.attributes.CurrentFamilyNameType;
import se.litsec.eidas.opensaml.ext.attributes.CurrentGivenNameType;
import se.litsec.eidas.opensaml.ext.attributes.DateOfBirthType;
import se.litsec.eidas.opensaml.ext.attributes.PersonIdentifierType;
import uk.gov.ida.notification.contracts.verifyserviceprovider.Attribute;
import uk.gov.ida.notification.contracts.verifyserviceprovider.Attributes;
import uk.gov.ida.notification.contracts.verifyserviceprovider.VspLevelOfAssurance;
import uk.gov.ida.notification.contracts.verifyserviceprovider.VspScenario;
import uk.gov.ida.notification.exceptions.hubresponse.HubResponseTranslationException;
import uk.gov.ida.notification.saml.EidasAttributeBuilder;
import uk.gov.ida.notification.saml.EidasResponseBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static uk.gov.ida.notification.contracts.verifyserviceprovider.Attributes.combineAttributeValues;

public class HubResponseTranslator {

    private String connectorNodeIssuerId;
    private String proxyNodeMetadataForConnectorNodeUrl;
    private Supplier<EidasResponseBuilder> eidasResponseBuilderSupplier;
    private String pidPrefix;

    public HubResponseTranslator(
            Supplier<EidasResponseBuilder> eidasResponseBuilderSupplier,
            String connectorNodeIssuerId,
            String proxyNodeMetadataForConnectorNodeUrl,
            String nationalityCode) {
        this.eidasResponseBuilderSupplier = eidasResponseBuilderSupplier;
        this.connectorNodeIssuerId = connectorNodeIssuerId;
        this.proxyNodeMetadataForConnectorNodeUrl = proxyNodeMetadataForConnectorNodeUrl;
        this.pidPrefix = String.format("UK/%s/", nationalityCode);

    }

    Response getTranslatedHubResponse(HubResponseContainer hubResponseContainer) {
        final List<EidasAttributeBuilder> eidasAttributeBuilders = new ArrayList<>();

        final String pid = pidPrefix + hubResponseContainer.getPid();

        if (hubResponseContainer.getVspScenario().equals(VspScenario.IDENTITY_VERIFIED)) {
            eidasAttributeBuilders.add(new EidasAttributeBuilder(
                    AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_NAME, AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_FRIENDLY_NAME, CurrentGivenNameType.TYPE_NAME,
                    getCombineFirstAndMiddleNames(hubResponseContainer)
            ));

            eidasAttributeBuilders.add(new EidasAttributeBuilder(
                    AttributeConstants.EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_NAME, AttributeConstants.EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_FRIENDLY_NAME, CurrentFamilyNameType.TYPE_NAME,
                    getCombinedSurnames(hubResponseContainer)
            ));

            eidasAttributeBuilders.add(new EidasAttributeBuilder(
                    AttributeConstants.EIDAS_DATE_OF_BIRTH_ATTRIBUTE_NAME, AttributeConstants.EIDAS_DATE_OF_BIRTH_ATTRIBUTE_FRIENDLY_NAME, DateOfBirthType.TYPE_NAME,
                    getLatestValidDateOfBirth(hubResponseContainer)
            ));

            eidasAttributeBuilders.add(new EidasAttributeBuilder(AttributeConstants.EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_NAME,
                AttributeConstants.EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_FRIENDLY_NAME,
                PersonIdentifierType.TYPE_NAME,
                pid
            ));
        }

        final DateTime now = DateTime.now();
        final List<org.opensaml.saml.saml2.core.Attribute> eidasAttributes = eidasAttributeBuilders
                .stream()
                .map(EidasAttributeBuilder::build)
                .collect(Collectors.toList());

        return eidasResponseBuilderSupplier.get()
                .withIssuer(proxyNodeMetadataForConnectorNodeUrl)
                .withStatus(getMappedStatusCode(hubResponseContainer.getVspScenario()))
                .withInResponseTo(hubResponseContainer.getEidasRequestId())
                .withIssueInstant(now)
                .withDestination(hubResponseContainer.getDestinationURL())
                .withAssertionSubject(pid)
                .withAssertionConditions(connectorNodeIssuerId)
                .withLoa(getMappedLoa(hubResponseContainer.getLevelOfAssurance()), now)
                .addAssertionAttributeStatement(eidasAttributes)
                .build();
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    private static String getMappedLoa(VspLevelOfAssurance vspLoa) {
        switch (vspLoa) {
            case LEVEL_2:
                return EidasConstants.EIDAS_LOA_SUBSTANTIAL;
            default:
                throw new HubResponseTranslationException("Received unsupported LOA from VSP: " + vspLoa);
        }
    }

    private static String getMappedStatusCode(VspScenario vspScenario) {
        switch (vspScenario) {
            case IDENTITY_VERIFIED:
                return StatusCode.SUCCESS;
            case CANCELLATION:
                return StatusCode.RESPONDER;
            case AUTHENTICATION_FAILED:
                return StatusCode.AUTHN_FAILED;
            case REQUEST_ERROR:
                throw new HubResponseTranslationException("Received error status from VSP: " + vspScenario);
            default:
                throw new HubResponseTranslationException("Received unknown status from VSP: " + vspScenario);
        }
    }

    private static String getCombineFirstAndMiddleNames(HubResponseContainer hubResponseContainer) {
        final List<Attribute<String>> firstNames = hubResponseContainer.getAttributes().getFirstNames();
        final List<Attribute<String>> middleNames = hubResponseContainer.getAttributes().getMiddleNames();

        final String firstNamesCombined = combineAttributeValues(getValidAttributes(firstNames));

        if (firstNamesCombined.isEmpty()) {
            throw new HubResponseTranslationException("No verified current first name present");
        }

        final String middleNamesCombined = combineAttributeValues(getValidAttributes(middleNames));

        return middleNamesCombined.isEmpty() ? firstNamesCombined : firstNamesCombined + " " + middleNamesCombined;
    }

    private static String getCombinedSurnames(HubResponseContainer hubResponseContainer) {
        return Optional.of(combineAttributeValues(getValidAttributes(hubResponseContainer.getAttributes().getSurnames())))
                .filter(s -> !s.isEmpty())
                .orElseThrow(() -> new HubResponseTranslationException("No verified current surname present"));
    }

    private static String getLatestValidDateOfBirth(HubResponseContainer hubResponseContainer) {
        return getValidAttributes(hubResponseContainer.getAttributes().getDatesOfBirth())
                .stream()
                .map(Attribute::getValue)
                .reduce(BinaryOperator.maxBy(DateTimeComparator.getDateOnlyInstance()))
                .map(Attributes::getFormattedDate)
                .orElseThrow(() -> new HubResponseTranslationException("No verified current date of birth present"));
    }

    private static <T> List<Attribute<T>> getValidAttributes(List<Attribute<T>> attributes) {
        return Optional.ofNullable(attributes).orElse(Collections.emptyList())
                .stream()
                .filter(Attribute::isValid)
                .collect(Collectors.toList());
    }
}
