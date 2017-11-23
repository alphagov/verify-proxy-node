package uk.gov.ida.notification.saml.translation;

import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.Response;
import se.litsec.eidas.opensaml.common.EidasConstants;
import se.litsec.eidas.opensaml.ext.attributes.AttributeConstants;
import se.litsec.eidas.opensaml.ext.attributes.CurrentFamilyNameType;
import se.litsec.eidas.opensaml.ext.attributes.CurrentGivenNameType;
import se.litsec.eidas.opensaml.ext.attributes.DateOfBirthType;
import se.litsec.eidas.opensaml.ext.attributes.PersonIdentifierType;
import uk.gov.ida.saml.core.IdaConstants;
import uk.gov.ida.saml.core.extensions.IdaAuthnContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HubResponseTranslator {
    public Response translate(Response response) {
        HubResponse hubResponse = new HubResponse(response);

        List<EidasAttributeBuilder> eidasAttributeBuilders = new ArrayList<>();

        eidasAttributeBuilders.add(new EidasAttributeBuilder(
                AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_NAME, AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_FRIENDLY_NAME, CurrentGivenNameType.TYPE_NAME,
                resp -> String.join(" ", resp.getMdsAttribute(IdaConstants.Attributes_1_1.Firstname.NAME), resp.getMdsAttribute(IdaConstants.Attributes_1_1.Middlename.NAME))
        ));

        eidasAttributeBuilders.add(new EidasAttributeBuilder(
                AttributeConstants.EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_NAME, AttributeConstants.EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_FRIENDLY_NAME, CurrentFamilyNameType.TYPE_NAME,
                resp -> resp.getMdsAttribute(IdaConstants.Attributes_1_1.Surname.NAME)
        ));

        eidasAttributeBuilders.add(new EidasAttributeBuilder(
                AttributeConstants.EIDAS_DATE_OF_BIRTH_ATTRIBUTE_NAME, AttributeConstants.EIDAS_DATE_OF_BIRTH_ATTRIBUTE_FRIENDLY_NAME, DateOfBirthType.TYPE_NAME,
                resp -> resp.getMdsAttribute(IdaConstants.Attributes_1_1.DateOfBirth.NAME)
        ));

        eidasAttributeBuilders.add(new EidasAttributeBuilder(AttributeConstants.EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_NAME, AttributeConstants.EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_FRIENDLY_NAME, PersonIdentifierType.TYPE_NAME,
                HubResponse::getPid
        ));

        List<Attribute> eidasAttributes = eidasAttributeBuilders
                .stream()
                .map(builder -> builder.build(hubResponse))
                .collect(Collectors.toList());

        String eidasLoa = mapLoa(hubResponse.getAuthnStatement().getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef());

        Response eidasResponse = new EidasResponseBuilder(
                hubResponse.getStatusCode(),
                hubResponse.getPid(),
                eidasLoa,
                eidasAttributes
        ).build();

        return eidasResponse;
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
