package uk.gov.ida.notification.eidassaml.saml.validation.components;

import org.junit.Test;
import se.litsec.eidas.opensaml.ext.RequestedAttribute;
import se.litsec.eidas.opensaml.ext.RequestedAttributes;
import uk.gov.ida.notification.exceptions.authnrequest.InvalidAuthnRequestException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.litsec.eidas.opensaml.ext.attributes.AttributeConstants.EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_NAME;
import static se.litsec.eidas.opensaml.ext.attributes.AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_NAME;
import static se.litsec.eidas.opensaml.ext.attributes.AttributeConstants.EIDAS_DATE_OF_BIRTH_ATTRIBUTE_NAME;
import static se.litsec.eidas.opensaml.ext.attributes.AttributeConstants.EIDAS_GENDER_ATTRIBUTE_NAME;
import static se.litsec.eidas.opensaml.ext.attributes.AttributeConstants.EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_NAME;

public class RequestedAttributesValidatorTest {

    @Test
    public void ignoreNonMandatoryAttributesThatAreNotRequired() {

        RequestedAttributes requestedAttributes = mock(RequestedAttributes.class);
        List<RequestedAttribute> mandatoryPlusGenderRequestedAttributes = List.of(
                setupAttribute(EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_NAME),
                setupAttribute(EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_NAME),
                setupAttribute(EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_NAME),
                setupAttribute(EIDAS_DATE_OF_BIRTH_ATTRIBUTE_NAME),
                setupAttribute(EIDAS_GENDER_ATTRIBUTE_NAME, false));

        when(requestedAttributes.getRequestedAttributes()).thenReturn(mandatoryPlusGenderRequestedAttributes);

        new RequestedAttributesValidator().validate(requestedAttributes);
    }

    @Test
    public void nonMandatoryAttributesCannotBeRequired() {

        RequestedAttributes requestedAttributes = mock(RequestedAttributes.class);
        List<RequestedAttribute> mandatoryPlusGenderRequestedAttributes = List.of(
                setupAttribute(EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_NAME),
                setupAttribute(EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_NAME),
                setupAttribute(EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_NAME),
                setupAttribute(EIDAS_DATE_OF_BIRTH_ATTRIBUTE_NAME),
                setupAttribute(EIDAS_GENDER_ATTRIBUTE_NAME));

        when(requestedAttributes.getRequestedAttributes()).thenReturn(mandatoryPlusGenderRequestedAttributes);

        assertThatThrownBy(() -> new RequestedAttributesValidator().validate(requestedAttributes))
                .isInstanceOf(InvalidAuthnRequestException.class)
                .hasMessageContaining("Non-mandatory RequestedAttribute should not be required");
    }

    @Test
    public void allMandatoryAttributesMustBeRequested() {

        RequestedAttributes requestedAttributes = mock(RequestedAttributes.class);
        List<RequestedAttribute> incompleteMandatoryAttributes = List.of(
                setupAttribute(EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_NAME),
                setupAttribute(EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_NAME),
                setupAttribute(EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_NAME));

        when(requestedAttributes.getRequestedAttributes()).thenReturn(incompleteMandatoryAttributes);

        assertThatThrownBy(() -> new RequestedAttributesValidator().validate(requestedAttributes))
                .isInstanceOf(InvalidAuthnRequestException.class)
                .hasMessageContaining("Missing mandatory RequestedAttribute")
                .hasMessageContaining("DateOfBirth");
    }

    @Test
    public void allMandatoryAttributesMustBeRequired() {

        RequestedAttributes requestedAttributes = mock(RequestedAttributes.class);
        List<RequestedAttribute> attributes = List.of(
                setupAttribute(EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_NAME),
                setupAttribute(EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_NAME),
                setupAttribute(EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_NAME),
                setupAttribute(EIDAS_DATE_OF_BIRTH_ATTRIBUTE_NAME, false));

        when(requestedAttributes.getRequestedAttributes()).thenReturn(attributes);

        assertThatThrownBy(() -> new RequestedAttributesValidator().validate(requestedAttributes))
                .isInstanceOf(InvalidAuthnRequestException.class)
                .hasMessageContaining("Mandatory RequestedAttribute needs to be required");
    }

    private RequestedAttribute setupAttribute(String attributeName) {
        return setupAttribute(attributeName, true);
    }

    private RequestedAttribute setupAttribute(String attributeName, boolean required) {
        RequestedAttribute attribute = mock(RequestedAttribute.class, attributeName);
        when(attribute.getName()).thenReturn(attributeName);
        when(attribute.isRequired()).thenReturn(required);
        when(attribute.getNameFormat()).thenReturn(RequestedAttribute.URI_REFERENCE);
        return attribute;
    }
}