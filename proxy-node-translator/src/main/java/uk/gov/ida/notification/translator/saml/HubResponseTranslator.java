package uk.gov.ida.notification.translator.saml;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;
import se.litsec.eidas.opensaml.common.EidasConstants;
import se.litsec.eidas.opensaml.ext.attributes.AttributeConstants;
import se.litsec.eidas.opensaml.ext.attributes.CurrentFamilyNameType;
import se.litsec.eidas.opensaml.ext.attributes.CurrentGivenNameType;
import se.litsec.eidas.opensaml.ext.attributes.DateOfBirthType;
import se.litsec.eidas.opensaml.ext.attributes.PersonIdentifierType;
import uk.gov.ida.notification.contracts.verifyserviceprovider.Attribute;
import uk.gov.ida.notification.contracts.verifyserviceprovider.VspLevelOfAssurance;
import uk.gov.ida.notification.contracts.verifyserviceprovider.VspScenario;
import uk.gov.ida.notification.exceptions.hubresponse.HubResponseTranslationException;
import uk.gov.ida.notification.saml.EidasAssertionBuilder;
import uk.gov.ida.notification.saml.EidasAttributeBuilder;
import uk.gov.ida.notification.saml.EidasResponseBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class HubResponseTranslator {

    private String connectorNodeIssuerId;
    private String proxyNodeMetadataForConnectorNodeUrl;
    private Supplier<EidasResponseBuilder> eidasResponseBuilderSupplier;

    public HubResponseTranslator(
            Supplier<EidasResponseBuilder> eidasResponseBuilderSupplier,
            String connectorNodeIssuerId,
            String proxyNodeMetadataForConnectorNodeUrl) {
        this.eidasResponseBuilderSupplier = eidasResponseBuilderSupplier;
        this.connectorNodeIssuerId = connectorNodeIssuerId;
        this.proxyNodeMetadataForConnectorNodeUrl = proxyNodeMetadataForConnectorNodeUrl;
    }

    private String combineFirstAndMiddleNames(Attribute<String> firstName, List<Attribute<String>> middleNames) {
        return firstName.getValue() + " " + combineAttributeValues(middleNames);
    }

    private String combineAttributeValues(List<Attribute<String>> attributes) {
        return attributes.stream()
                .filter(Objects::nonNull)
                .map(Attribute::getValue)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(" "));
    }

    public Response translate(HubResponseContainer hubResponseContainer) {

        validateHubResponseContainerAttributes(hubResponseContainer);

        List<EidasAttributeBuilder> eidasAttributeBuilders = new ArrayList<>();

        if (hubResponseContainer.getVspScenario().equals(VspScenario.IDENTITY_VERIFIED)) {
            eidasAttributeBuilders.add(new EidasAttributeBuilder(
                    AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_NAME, AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_FRIENDLY_NAME, CurrentGivenNameType.TYPE_NAME,
                    combineFirstAndMiddleNames(hubResponseContainer.getAttributes().getFirstName(), hubResponseContainer.getAttributes().getMiddleNames())
            ));

            eidasAttributeBuilders.add(new EidasAttributeBuilder(
                    AttributeConstants.EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_NAME, AttributeConstants.EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_FRIENDLY_NAME, CurrentFamilyNameType.TYPE_NAME,
                    combineAttributeValues(hubResponseContainer.getAttributes().getSurnames())
            ));

            eidasAttributeBuilders.add(new EidasAttributeBuilder(
                    AttributeConstants.EIDAS_DATE_OF_BIRTH_ATTRIBUTE_NAME, AttributeConstants.EIDAS_DATE_OF_BIRTH_ATTRIBUTE_FRIENDLY_NAME, DateOfBirthType.TYPE_NAME,
                    hubResponseContainer.getAttributes().getDateOfBirth().getValue().toString(DateTimeFormat.forPattern("YYYY-MM-dd"))
            ));

            eidasAttributeBuilders.add(new EidasAttributeBuilder(AttributeConstants.EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_NAME, AttributeConstants.EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_FRIENDLY_NAME, PersonIdentifierType.TYPE_NAME,
                    EidasAssertionBuilder.TEMPORARY_PID_TRANSLATION + hubResponseContainer.getPid()
            ));
        }

        List<org.opensaml.saml.saml2.core.Attribute> eidasAttributes = eidasAttributeBuilders
                .stream()
                .map(EidasAttributeBuilder::build)
                .collect(Collectors.toList());


        DateTime now = DateTime.now();
        return eidasResponseBuilderSupplier.get()
                .withIssuer(proxyNodeMetadataForConnectorNodeUrl)
                .withStatus(getMappedStatusCode(hubResponseContainer.getVspScenario()))
                .withInResponseTo(hubResponseContainer.getEidasRequestId())
                .withIssueInstant(now)
                .withDestination(hubResponseContainer.getDestinationURL())
                .withAssertionSubject(hubResponseContainer.getPid())
                .addAssertionAuthnStatement(getMappedLoa(hubResponseContainer.getLevelOfAssurance()), now)
                .addAssertionAttributeStatement(eidasAttributes)
                .withAssertionConditions(connectorNodeIssuerId)
                .build();

    }

    private void validateHubResponseContainerAttributes(HubResponseContainer hubResponseContainer) {
        if ( hubResponseContainer == null ) {
            throw new HubResponseTranslationException("HubResponseContainer is null.");
        }
        if (hubResponseContainer.getVspScenario() != null && hubResponseContainer.getVspScenario() == VspScenario.IDENTITY_VERIFIED) {
            if (hubResponseContainer.getAttributes() == null) {
                throw new HubResponseTranslationException("HubResponseContainer Attributes null.");
            }
            if (hubResponseContainer.getAttributes().getFirstName() == null) {
                throw new HubResponseTranslationException("HubResponseContainer Attribute FirstName null.");
            }
            if (hubResponseContainer.getAttributes().getSurnames() == null) {
                throw new HubResponseTranslationException("HubResponseContainer Attribute Surnames null.");
            }
            if (hubResponseContainer.getAttributes().getDateOfBirth() == null
                    && hubResponseContainer.getAttributes().getDateOfBirth().getValue() == null) {
                throw new HubResponseTranslationException("HubResponseContainer Attribute DateOfBirth null or missing value.");
            }
            if (hubResponseContainer.getPid() == null) {
                throw new HubResponseTranslationException("HubResponseContainer Attribute Pid null.");
            }
        }
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    private String getMappedLoa(VspLevelOfAssurance vspLoa) {
        switch (vspLoa) {
            case LEVEL_2:
                return EidasConstants.EIDAS_LOA_SUBSTANTIAL;
            default:
                throw new HubResponseTranslationException("Received unsupported LOA from VSP: " + vspLoa);
        }
    }

    private String getMappedStatusCode(VspScenario vspScenario) {
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
}