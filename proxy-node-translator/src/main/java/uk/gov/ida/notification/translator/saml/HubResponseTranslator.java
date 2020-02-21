package uk.gov.ida.notification.translator.saml;

import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;
import se.litsec.eidas.opensaml.common.EidasConstants;
import se.litsec.eidas.opensaml.ext.attributes.AttributeConstants;
import se.litsec.eidas.opensaml.ext.attributes.CurrentFamilyNameType;
import se.litsec.eidas.opensaml.ext.attributes.CurrentGivenNameType;
import se.litsec.eidas.opensaml.ext.attributes.DateOfBirthType;
import se.litsec.eidas.opensaml.ext.attributes.PersonIdentifierType;
import uk.gov.ida.notification.contracts.metadata.CountryMetadataResponse;
import uk.gov.ida.notification.contracts.verifyserviceprovider.Attributes;
import uk.gov.ida.notification.contracts.verifyserviceprovider.VspLevelOfAssurance;
import uk.gov.ida.notification.contracts.verifyserviceprovider.VspScenario;
import uk.gov.ida.notification.exceptions.hubresponse.HubResponseTranslationException;
import uk.gov.ida.notification.saml.EidasAttributeBuilder;
import uk.gov.ida.notification.saml.EidasResponseBuilder;
import uk.gov.ida.saml.core.domain.NonMatchingVerifiableAttribute;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class HubResponseTranslator {

    private static final String PID_PREFIX = "GB/%s/%s";
    private String proxyNodeMetadataForConnectorNodeUrl;
    private Supplier<EidasResponseBuilder> eidasResponseBuilderSupplier;


    public HubResponseTranslator(
            Supplier<EidasResponseBuilder> eidasResponseBuilderSupplier,
            String proxyNodeMetadataForConnectorNodeUrl) {
        this.eidasResponseBuilderSupplier = eidasResponseBuilderSupplier;
        this.proxyNodeMetadataForConnectorNodeUrl = proxyNodeMetadataForConnectorNodeUrl;
    }

    Response getTranslatedHubResponse(HubResponseContainer hubResponseContainer, CountryMetadataResponse countryMetadataResponse) {
        final List<EidasAttributeBuilder> eidasAttributeBuilders = new ArrayList<>();

        final String pid = hubResponseContainer.getPid()
                .map(p -> String.format(PID_PREFIX, countryMetadataResponse.getCountryCode(), p))
                .orElse(null);

        if (hubResponseContainer.getVspScenario().equals(VspScenario.IDENTITY_VERIFIED)) {
            var attributes = hubResponseContainer
                    .getAttributes()
                    .orElseThrow(
                            () -> new HubResponseTranslationException("Attributes are null for VSP scenario: " + hubResponseContainer.getVspScenario())
                    );

            eidasAttributeBuilders.add(new EidasAttributeBuilder(
                    AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_NAME, AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_FRIENDLY_NAME, CurrentGivenNameType.TYPE_NAME,
                    getCombineFirstAndMiddleNames(attributes)
            ));

            eidasAttributeBuilders.add(new EidasAttributeBuilder(
                    AttributeConstants.EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_NAME, AttributeConstants.EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_FRIENDLY_NAME, CurrentFamilyNameType.TYPE_NAME,
                    getCombinedSurnames(attributes)
            ));

            eidasAttributeBuilders.add(new EidasAttributeBuilder(
                    AttributeConstants.EIDAS_DATE_OF_BIRTH_ATTRIBUTE_NAME, AttributeConstants.EIDAS_DATE_OF_BIRTH_ATTRIBUTE_FRIENDLY_NAME, DateOfBirthType.TYPE_NAME,
                    getLatestValidDateOfBirth(attributes)
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
                .withAssertionConditions(countryMetadataResponse.getEntityId())
                .withLoa(getMappedLoa(hubResponseContainer.getLevelOfAssurance()), now)
                .addAssertionAttributeStatement(eidasAttributes)
                .build();
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    private static String getMappedLoa(Optional<VspLevelOfAssurance> vspLoa) {
        if (vspLoa.isEmpty()) {
            return null;
        }
        switch (vspLoa.get()) {
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

    private static String getCombineFirstAndMiddleNames(Attributes attributes) {
        var firstNames = attributes.getFirstNamesAttributesList();
        var validFirstNames = firstNames.getValidAttributes();
        if (validFirstNames.isEmpty()) {
            throw new HubResponseTranslationException("No verified current first name present: " + firstNames.createAttributesMessage());
        }
        var validMiddleNames = attributes.getMiddleNamesAttributesList().getValidAttributes();
        validFirstNames.addAll(validMiddleNames);

        return combineStringAttributeValues(validFirstNames);
    }

    private static String getCombinedSurnames(Attributes attributes) {
        var surnames = attributes.getSurnamesAttributesList();
        var validSurnames = surnames.getValidAttributes();
        if (validSurnames.isEmpty()) {
            throw new HubResponseTranslationException("No verified current surname present: " + surnames.createAttributesMessage());
        }
        return combineStringAttributeValues(surnames.getValidAttributes());
    }

    private static String getLatestValidDateOfBirth(Attributes attributes) {
        var datesOfBirth = attributes.getDatesOfBirthAttributesList();
        var validDatesOfBirth = datesOfBirth.getValidAttributes();
        if (validDatesOfBirth.isEmpty()) {
            throw new HubResponseTranslationException("No verified current date of birth present: " + datesOfBirth.createAttributesMessage());
        }

        return validDatesOfBirth.stream()
                .map(NonMatchingVerifiableAttribute::getValue)
                .reduce(BinaryOperator.maxBy(Comparator.comparing(LocalDate::toEpochDay)))
                .map(LocalDate::toString).get();
    }

    private static String combineStringAttributeValues(List<NonMatchingVerifiableAttribute<String>> attributeStream) {
        return attributeStream.stream().map(NonMatchingVerifiableAttribute::getValue).filter(s -> !s.isEmpty()).collect(Collectors.joining(" "));
    }
}
