package uk.gov.ida.notification.saml.validation.components;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.AuthnRequest;
import se.litsec.eidas.opensaml.ext.RequestedAttribute;
import se.litsec.eidas.opensaml.ext.RequestedAttributes;
import uk.gov.ida.notification.exceptions.authnrequest.InvalidAuthnRequestException;
import uk.gov.ida.notification.helpers.EidasAuthnRequestBuilder;

import static se.litsec.eidas.opensaml.ext.attributes.AttributeConstants.EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_NAME;
import static se.litsec.eidas.opensaml.ext.attributes.AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_NAME;
import static se.litsec.eidas.opensaml.ext.attributes.AttributeConstants.EIDAS_DATE_OF_BIRTH_ATTRIBUTE_NAME;
import static se.litsec.eidas.opensaml.ext.attributes.AttributeConstants.EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_NAME;

public class RequestedAttributesValidatorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static RequestedAttributesValidator requestedAttributesValidator;
    private EidasAuthnRequestBuilder eidasAuthnRequestBuilder;

    @BeforeClass
    public static void classSetup() throws Throwable {
        InitializationService.initialize();
        requestedAttributesValidator = new RequestedAttributesValidator();
    }

    @Before
    public void setUp() throws Throwable {
        eidasAuthnRequestBuilder = new EidasAuthnRequestBuilder();
    }

    @Test
    public void shouldNotThrowExceptionIfValidRequestedAttributes() throws Throwable {
        AuthnRequest request = eidasAuthnRequestBuilder.build();
        requestedAttributesValidator.validate(getRequestedAttributes(request));
    }

    @Test
    public void shouldThrowExceptionIfNoRequestedAttributes() throws Throwable {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Missing RequestedAttributes");

        AuthnRequest request = eidasAuthnRequestBuilder.withoutRequestedAttributes().build();
        requestedAttributesValidator.validate(getRequestedAttributes(request));
    }

    @Test
    public void shouldThrowExceptionIfInvalidRequestedAttributeNameFormat() throws Throwable {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Invalid RequestedAttribute NameFormat 'invalid'");

        ImmutableMap<String, String> xmlAttributes = ImmutableMap.of(RequestedAttribute.NAME_FORMAT_ATTRIB_NAME, "invalid");
        AuthnRequest request = eidasAuthnRequestBuilder.withRequestedAttribute("OptionalAttribute", xmlAttributes).build();
        requestedAttributesValidator.validate(getRequestedAttributes(request));
    }
    
    @Test
    public void shouldAllowOptionalNonMandatoryAttributeWithNoIsRequired() throws Throwable {
        ImmutableMap<String, String> xmlAttributes = ImmutableMap.of(RequestedAttribute.NAME_FORMAT_ATTRIB_NAME, RequestedAttribute.URI_REFERENCE);
        AuthnRequest request = eidasAuthnRequestBuilder.withRequestedAttribute("OptionalAttribute", xmlAttributes).build();
        requestedAttributesValidator.validate(getRequestedAttributes(request));
    }

    @Test
    public void shouldAllowOptionalNonMandatoryAttributeWithIsRequiredFalse() throws Throwable {
        ImmutableMap<String, String> xmlAttributes = ImmutableMap.of(
                RequestedAttribute.NAME_FORMAT_ATTRIB_NAME, RequestedAttribute.URI_REFERENCE,
                RequestedAttribute.IS_REQUIRED_ATTRIB_NAME, "false"
        );
        AuthnRequest request = eidasAuthnRequestBuilder.withRequestedAttribute("OptionalAttribute", xmlAttributes).build();
        requestedAttributesValidator.validate(getRequestedAttributes(request));
    }

    @Test
    public void shouldThrowExceptionIfNonMandatoryAttributeIsRequired() throws Throwable {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Non-mandatory RequestedAttribute should not be required");

        ImmutableMap<String, String> xmlAttributes = ImmutableMap.of(
                RequestedAttribute.NAME_FORMAT_ATTRIB_NAME, RequestedAttribute.URI_REFERENCE,
                RequestedAttribute.IS_REQUIRED_ATTRIB_NAME, "true"
        );
        AuthnRequest request = eidasAuthnRequestBuilder.withRequestedAttribute("OptionalAttribute", xmlAttributes).build();
        requestedAttributesValidator.validate(getRequestedAttributes(request));
    }
    
    @Test
    public void shouldThrowExceptionIfNoPersonIdentifierAttribute() throws Throwable {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Missing mandatory RequestedAttribute");

        AuthnRequest request = eidasAuthnRequestBuilder.withoutRequestedAttribute(EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_NAME).build();
        requestedAttributesValidator.validate(getRequestedAttributes(request));
    }

    @Test
    public void shouldThrowExceptionIfPersonIdentifierAttributeNotRequired() throws Throwable {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Mandatory RequestedAttribute needs to be required '" + EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_NAME + "'");

        AuthnRequest request = buildAuthnRequestWithNonRequired(EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_NAME);
        requestedAttributesValidator.validate(getRequestedAttributes(request));
    }

    @Test
    public void shouldThrowExceptionIfNoGivenNameAttribute() throws Throwable {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Missing mandatory RequestedAttribute");

        AuthnRequest request = eidasAuthnRequestBuilder.withoutRequestedAttribute(EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_NAME).build();
        requestedAttributesValidator.validate(getRequestedAttributes(request));
    }

    @Test
    public void shouldThrowExceptionIfGivenNameAttributeNotRequired() throws Throwable {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Mandatory RequestedAttribute needs to be required '" + EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_NAME + "'");

        AuthnRequest request = buildAuthnRequestWithNonRequired(EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_NAME);
        requestedAttributesValidator.validate(getRequestedAttributes(request));
    }

    @Test
    public void shouldThrowExceptionIfNoFamilyNameAttribute() throws Throwable {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Missing mandatory RequestedAttribute");

        AuthnRequest request = eidasAuthnRequestBuilder.withoutRequestedAttribute(EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_NAME).build();
        requestedAttributesValidator.validate(getRequestedAttributes(request));
    }

    @Test
    public void shouldThrowExceptionIfFamilyNameAttributeNotRequired() throws Throwable {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Mandatory RequestedAttribute needs to be required '" + EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_NAME + "'");

        AuthnRequest request = buildAuthnRequestWithNonRequired(EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_NAME);
        requestedAttributesValidator.validate(getRequestedAttributes(request));
    }

    @Test
    public void shouldThrowExceptionIfNoDateOfBirthAttribute() throws Throwable {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Missing mandatory RequestedAttribute");

        AuthnRequest request = eidasAuthnRequestBuilder.withoutRequestedAttribute(EIDAS_DATE_OF_BIRTH_ATTRIBUTE_NAME).build();
        requestedAttributesValidator.validate(getRequestedAttributes(request));
    }

    @Test
    public void shouldThrowExceptionIfDateOfBirthAttributeNotRequired() throws Throwable {
        expectedException.expect(InvalidAuthnRequestException.class);
        expectedException.expectMessage("Bad Authn Request from Connector Node: Mandatory RequestedAttribute needs to be required '" + EIDAS_DATE_OF_BIRTH_ATTRIBUTE_NAME + "'");

        AuthnRequest request = buildAuthnRequestWithNonRequired(EIDAS_DATE_OF_BIRTH_ATTRIBUTE_NAME);
        requestedAttributesValidator.validate(getRequestedAttributes(request));
    }

    private AuthnRequest buildAuthnRequestWithNonRequired(String attributeName) throws Throwable {
        ImmutableMap<String, String> xmlAttributes = ImmutableMap.of(RequestedAttribute.IS_REQUIRED_ATTRIB_NAME, "false");
        return eidasAuthnRequestBuilder.withRequestedAttribute(attributeName, xmlAttributes).build();
    }

    private RequestedAttributes getRequestedAttributes(AuthnRequest request) {
        return (RequestedAttributes) request
                .getExtensions()
                .getUnknownXMLObjects(RequestedAttributes.DEFAULT_ELEMENT_NAME)
                .stream().findFirst().orElse(null);
    }
}
