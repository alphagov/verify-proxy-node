package uk.gov.ida.notification.saml.translation;

import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.Response;
import se.litsec.eidas.opensaml.common.EidasConstants;
import se.litsec.eidas.opensaml.ext.attributes.AttributeConstants;
import se.litsec.eidas.opensaml.ext.attributes.CurrentFamilyNameType;
import se.litsec.eidas.opensaml.ext.attributes.CurrentGivenNameType;
import se.litsec.eidas.opensaml.ext.attributes.DateOfBirthType;
import se.litsec.eidas.opensaml.ext.attributes.PersonIdentifierType;
import uk.gov.ida.notification.exceptions.HubResponseException;
import uk.gov.ida.saml.core.IdaConstants;
import uk.gov.ida.saml.core.extensions.Date;
import uk.gov.ida.saml.core.extensions.IdaAuthnContext;
import uk.gov.ida.saml.core.extensions.PersonName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HubResponseTranslator {

    private EidasResponseBuilder eidasResponseBuilder;
    private String destinationUrl;
    private String proxyNodeMetadataForConnectorNodeUrl;

    public HubResponseTranslator(EidasResponseBuilder eidasResponseBuilder, String destinationUrl, String proxyNodeMetadataForConnectorNodeUrl) {
        this.eidasResponseBuilder = eidasResponseBuilder;
        this.destinationUrl = destinationUrl;
        this.proxyNodeMetadataForConnectorNodeUrl = proxyNodeMetadataForConnectorNodeUrl;
    }

    public Response translate(HubResponseContainer hubResponseContainer) {
        List<EidasAttributeBuilder> eidasAttributeBuilders = new ArrayList<>();

        eidasAttributeBuilders.add(new EidasAttributeBuilder(
                AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_NAME, AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_FRIENDLY_NAME, CurrentGivenNameType.TYPE_NAME,
                combineFirstAndMiddleNames(hubResponseContainer)
        ));

        eidasAttributeBuilders.add(new EidasAttributeBuilder(
                AttributeConstants.EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_NAME, AttributeConstants.EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_FRIENDLY_NAME, CurrentFamilyNameType.TYPE_NAME,
                hubResponseContainer.getMdsAssertion().getMdsAttribute(IdaConstants.Attributes_1_1.Surname.NAME, PersonName.class).getValue()
        ));

        eidasAttributeBuilders.add(new EidasAttributeBuilder(
                AttributeConstants.EIDAS_DATE_OF_BIRTH_ATTRIBUTE_NAME, AttributeConstants.EIDAS_DATE_OF_BIRTH_ATTRIBUTE_FRIENDLY_NAME, DateOfBirthType.TYPE_NAME,
                hubResponseContainer.getMdsAssertion().getMdsAttribute(IdaConstants.Attributes_1_1.DateOfBirth.NAME, Date.class).getValue()
        ));

        eidasAttributeBuilders.add(new EidasAttributeBuilder(AttributeConstants.EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_NAME, AttributeConstants.EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_FRIENDLY_NAME, PersonIdentifierType.TYPE_NAME,
                EidasResponseBuilder.TEMPORARY_PID_TRANSLATION + hubResponseContainer.getAuthnAssertion().getPid()
        ));

        List<Attribute> eidasAttributes = eidasAttributeBuilders
                .stream()
                .map(EidasAttributeBuilder::build)
                .collect(Collectors.toList());

        String eidasLoa = mapLoa(hubResponseContainer.getAuthnAssertion().getProvidedLoa());

        return eidasResponseBuilder.createEidasResponse(
                proxyNodeMetadataForConnectorNodeUrl,
                hubResponseContainer.getHubResponse().getStatusCode(),
                hubResponseContainer.getAuthnAssertion().getPid(),
                eidasLoa,
                eidasAttributes,
                hubResponseContainer.getHubResponse().getInResponseTo(),
                hubResponseContainer.getHubResponse().getIssueInstant(),
                hubResponseContainer.getMdsAssertion().getIssueInstant(),
                hubResponseContainer.getAuthnAssertion().getAuthnInstant(),
                destinationUrl
                );
    }

    private String combineFirstAndMiddleNames(HubResponseContainer hubResponseContainer) {
        List<PersonName> names = Arrays.asList(
                hubResponseContainer.getMdsAssertion().getMdsAttribute(IdaConstants.Attributes_1_1.Firstname.NAME, PersonName.class),
                hubResponseContainer.getMdsAssertion().getMdsAttribute(IdaConstants.Attributes_1_1.Middlename.NAME, PersonName.class));
        return names.stream()
                .filter(Objects::nonNull)
                .map(PersonName::getValue)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(" "));
    }

    private String mapLoa(String hubLoa) {
        switch(hubLoa) {
            case IdaAuthnContext.LEVEL_2_AUTHN_CTX:
                return EidasConstants.EIDAS_LOA_SUBSTANTIAL;
            default:
                throw new HubResponseException("Invalid level of assurance: " + hubLoa);
        }
    }
}
