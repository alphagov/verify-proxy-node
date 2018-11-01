package uk.gov.ida.notification.saml.validation.components;

import com.google.common.collect.ImmutableList;
import org.apache.commons.collections.ListUtils;
import org.opensaml.saml.saml2.core.Attribute;
import se.litsec.eidas.opensaml.ext.RequestedAttribute;
import se.litsec.eidas.opensaml.ext.RequestedAttributes;
import uk.gov.ida.notification.exceptions.authnrequest.InvalidAuthnRequestException;
import uk.gov.ida.notification.exceptions.hubresponse.InvalidHubResponseException;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

import static se.litsec.eidas.opensaml.ext.attributes.AttributeConstants.EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_NAME;
import static se.litsec.eidas.opensaml.ext.attributes.AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_NAME;
import static se.litsec.eidas.opensaml.ext.attributes.AttributeConstants.EIDAS_DATE_OF_BIRTH_ATTRIBUTE_NAME;
import static se.litsec.eidas.opensaml.ext.attributes.AttributeConstants.EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_NAME;

public class RequestedAttributesValidator {

    private final ImmutableList<String> mandatoryAttributes = ImmutableList.of(
            EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_NAME,
            EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_NAME,
            EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_NAME,
            EIDAS_DATE_OF_BIRTH_ATTRIBUTE_NAME
    );

    public void validate(RequestedAttributes requestedAttributesParent) {
        if (requestedAttributesParent == null) throw new InvalidAuthnRequestException("Missing RequestedAttributes");

        List<RequestedAttribute> requestedAttributes = requestedAttributesParent.getRequestedAttributes();

        List<String> requestedAttributesNames = requestedAttributes.stream()
                .map(this::validateRequestedAttribute)
                .map(Attribute::getName)
                .collect(Collectors.toList());

        List missingMandatoryAttributes = ListUtils.subtract(mandatoryAttributes, requestedAttributesNames);
        if (!missingMandatoryAttributes.isEmpty())
            throw new InvalidAuthnRequestException(MessageFormat.format("Missing mandatory RequestedAttribute(s): {0}", String.join(", ", missingMandatoryAttributes)));
    }

    private RequestedAttribute validateRequestedAttribute(RequestedAttribute requestedAttribute) {
        if(!RequestedAttribute.URI_REFERENCE.equals(requestedAttribute.getNameFormat()))
            throw new InvalidAuthnRequestException(nameFormatError(requestedAttribute));

        if(mandatoryAttributes.contains(requestedAttribute.getName()) && !requestedAttribute.isRequired())
            throw new InvalidAuthnRequestException(mandatoryAttributeError(requestedAttribute));

        if(!mandatoryAttributes.contains(requestedAttribute.getName()) && requestedAttribute.isRequired())
            throw new InvalidAuthnRequestException(optionalAttributeError(requestedAttribute));

        return requestedAttribute;
    }

    private String nameFormatError(RequestedAttribute requestedAttribute) {
        return MessageFormat.format("Invalid RequestedAttribute NameFormat ''{0}''", requestedAttribute.getNameFormat());
    }

    private String mandatoryAttributeError(RequestedAttribute requestedAttribute) {
        return MessageFormat.format("Mandatory RequestedAttribute needs to be required ''{0}''", requestedAttribute.getName());
    }

    private String optionalAttributeError(RequestedAttribute requestedAttribute) {
        return MessageFormat.format("Non-mandatory RequestedAttribute should not be required ''{0}''", requestedAttribute.getName());
    }
}

