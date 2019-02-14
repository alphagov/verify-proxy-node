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
import uk.gov.ida.notification.contracts.verifyserviceprovider.Attribute;
import uk.gov.ida.notification.contracts.verifyserviceprovider.VspLevelOfAssurance;
import uk.gov.ida.notification.contracts.verifyserviceprovider.VspScenario;
import uk.gov.ida.notification.exceptions.hubresponse.HubResponseTranslationException;
import uk.gov.ida.notification.saml.EidasAssertionBuilder;
import uk.gov.ida.notification.saml.EidasAttributeBuilder;
import uk.gov.ida.notification.saml.EidasResponseBuilder;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HubResponseTranslator {

    private String connectorNodeIssuerId;
    private String proxyNodeMetadataForConnectorNodeUrl;

    public HubResponseTranslator(String connectorNodeIssuerId, String proxyNodeMetadataForConnectorNodeUrl) {
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
        List<EidasAttributeBuilder> eidasAttributeBuilders = new ArrayList<>();

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
                hubResponseContainer.getAttributes().getDateOfBirth().getValue().format(DateTimeFormatter.ofPattern("YYYY-MM-dd"))
        ));

        eidasAttributeBuilders.add(new EidasAttributeBuilder(AttributeConstants.EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_NAME, AttributeConstants.EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_FRIENDLY_NAME, PersonIdentifierType.TYPE_NAME,
                EidasAssertionBuilder.TEMPORARY_PID_TRANSLATION + hubResponseContainer.getPid()
        ));

        List<org.opensaml.saml.saml2.core.Attribute> eidasAttributes = eidasAttributeBuilders
                .stream()
                .map(EidasAttributeBuilder::build)
                .collect(Collectors.toList());

        return EidasResponseBuilder.createEidasResponse(
                proxyNodeMetadataForConnectorNodeUrl,
                getMappedStatusCode(hubResponseContainer.getVspScenario()),
                hubResponseContainer.getPid(),
                getMappedLoa(hubResponseContainer.getLevelOfAssurance()),
                eidasAttributes,
                hubResponseContainer.getEidasRequestId(),
                DateTime.now(),
                DateTime.now(),
                DateTime.now(),
                hubResponseContainer.getDestinationURL(),
                connectorNodeIssuerId
        );
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
