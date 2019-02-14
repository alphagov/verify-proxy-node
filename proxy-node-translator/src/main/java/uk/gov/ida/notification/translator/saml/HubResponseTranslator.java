package uk.gov.ida.notification.translator.saml;

import org.opensaml.saml.saml2.core.Response;
import se.litsec.eidas.opensaml.common.EidasConstants;
import se.litsec.eidas.opensaml.ext.attributes.AttributeConstants;
import se.litsec.eidas.opensaml.ext.attributes.CurrentFamilyNameType;
import se.litsec.eidas.opensaml.ext.attributes.CurrentGivenNameType;
import se.litsec.eidas.opensaml.ext.attributes.DateOfBirthType;
import se.litsec.eidas.opensaml.ext.attributes.PersonIdentifierType;
import uk.gov.ida.notification.contracts.verifyserviceprovider.Attribute;
import uk.gov.ida.notification.contracts.verifyserviceprovider.TranslatedHubResponse;
import uk.gov.ida.notification.exceptions.hubresponse.HubResponseTranslationException;
import uk.gov.ida.notification.saml.EidasAssertionBuilder;
import uk.gov.ida.notification.saml.EidasAttributeBuilder;
import uk.gov.ida.notification.saml.EidasResponseBuilder;
import uk.gov.ida.saml.core.IdaConstants;
import uk.gov.ida.saml.core.extensions.Date;
import uk.gov.ida.saml.core.extensions.IdaAuthnContext;
import uk.gov.ida.saml.core.extensions.PersonName;
import uk.gov.ida.saml.core.extensions.impl.PersonNameBuilder;


import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HubResponseTranslator {

    private String connectorNodeIssuerId;
    private String destinationUrl;
    private String proxyNodeMetadataForConnectorNodeUrl;

    public HubResponseTranslator(String connectorNodeIssuerId, String destinationUrl, String proxyNodeMetadataForConnectorNodeUrl) {
        this.connectorNodeIssuerId = connectorNodeIssuerId;
        this.destinationUrl = destinationUrl;
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

        String eidasLoa = mapLoa(hubResponseContainer.getLevelOfAssurance());

        // TODO: The missing attributes will be added in a separate PR
        return EidasResponseBuilder.createEidasResponse(
                proxyNodeMetadataForConnectorNodeUrl,
                "" /* hubResponseContainer.getHubResponse().getStatusCode() */,
                hubResponseContainer.getPid(),
                eidasLoa,
                eidasAttributes,
                hubResponseContainer.getEidasRequestId(),
                null /* hubResponseContainer.getHubResponse().getIssueInstant() */,
                null /* hubResponseContainer.getMdsAssertion().getIssueInstant() */,
                null /* hubResponseContainer.getAuthnAssertion().getAuthnInstant()*/,
                destinationUrl,
                connectorNodeIssuerId
        );
    }

    // TODO: The LOA translation will be handed in a separate PR
    private String mapLoa(String hubLoa) {
    //    switch(hubLoa) {
    //        case IdaAuthnContext.LEVEL_2_AUTHN_CTX:
                return EidasConstants.EIDAS_LOA_SUBSTANTIAL;
    //        default:
    //            throw new HubResponseTranslationException("Invalid level of assurance: " + hubLoa);
    //    }
    }
}
