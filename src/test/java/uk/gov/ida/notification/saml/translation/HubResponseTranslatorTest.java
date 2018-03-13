package uk.gov.ida.notification.saml.translation;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Response;
import se.litsec.eidas.opensaml.common.EidasConstants;
import se.litsec.eidas.opensaml.ext.attributes.AttributeConstants;
import se.litsec.eidas.opensaml.ext.attributes.impl.CurrentFamilyNameTypeImpl;
import se.litsec.eidas.opensaml.ext.attributes.impl.CurrentGivenNameTypeImpl;
import se.litsec.eidas.opensaml.ext.attributes.impl.DateOfBirthTypeImpl;
import se.litsec.eidas.opensaml.ext.attributes.impl.PersonIdentifierTypeImpl;
import uk.gov.ida.notification.SamlInitializedTest;
import uk.gov.ida.saml.core.IdaConstants;
import uk.gov.ida.saml.core.extensions.IdaAuthnContext;
import uk.gov.ida.saml.core.test.builders.DateAttributeValueBuilder;
import uk.gov.ida.saml.core.test.builders.PersonNameAttributeValueBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HubResponseTranslatorTest extends SamlInitializedTest {

    @Test
    public void shouldTranslateVerifyTermsIntoEidasEquivalentsWhenGeneratingEidasResponse() {
        HubResponseTranslator hubResponseTranslator = new HubResponseTranslator("connectorNodeIssuerId", "www.example.com/response/POST", "issuerId");
        DateTime dummyTime = DateTime.now();
        HubResponseContainer hubResponseContainer = new HubResponseContainer(
                new HubResponse("success", "response id", "id of request", dummyTime),
                new HubMdsAssertion(buildHubAttributes("Jane", "Doe","Smith", "1984-02-29"), dummyTime),
                new HubAuthnAssertion("pid", IdaAuthnContext.LEVEL_2_AUTHN_CTX, dummyTime)
        );

        Response response = hubResponseTranslator.translate(hubResponseContainer);
        Assertion assertion = response.getAssertions().get(0);
        List<Attribute> attributes = assertion.getAttributeStatements().get(0).getAttributes();
        AuthnStatement authnStatement = assertion.getAuthnStatements().get(0);

        assertEquals("issuerId", response.getIssuer().getValue());
        assertEquals("success", response.getStatus().getStatusCode().getValue());
        assertEquals("UK/EU/pid", assertion.getSubject().getNameID().getValue());
        assertEquals(EidasConstants.EIDAS_LOA_SUBSTANTIAL, authnStatement.getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef());
        assertEquals("id of request", response.getInResponseTo());
        assertTrue(dummyTime.isEqual(response.getIssueInstant()));
        assertTrue(dummyTime.isEqual(assertion.getIssueInstant()));
        assertTrue(dummyTime.isEqual(authnStatement.getAuthnInstant()));
        assertEquals("www.example.com/response/POST", response.getDestination());
        assertEquals("issuerId", assertion.getIssuer().getValue());
        assertEquals(4, attributes.size());

        assertEquals("Jane Doe",
                ((CurrentGivenNameTypeImpl) extractAttribute(attributes, AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_NAME)).getValue());
        assertEquals("Smith",
                ((CurrentFamilyNameTypeImpl) extractAttribute(attributes, AttributeConstants.EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_NAME)).getValue());
        assertEquals(new LocalDate(1984, 2, 29),
                ((DateOfBirthTypeImpl) extractAttribute(attributes, AttributeConstants.EIDAS_DATE_OF_BIRTH_ATTRIBUTE_NAME)).getDate());
        assertEquals(EidasAssertionBuilder.TEMPORARY_PID_TRANSLATION + "pid",
                ((PersonIdentifierTypeImpl) extractAttribute(attributes, AttributeConstants.EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_NAME)).getValue());
    }

    private Map<String, AttributeValue> buildHubAttributes(String firstName, String middleName, String surname, String dob) {
        Map<String, AttributeValue> hubResponseAttributes = new HashMap<>();

        hubResponseAttributes.put(
                IdaConstants.Attributes_1_1.Firstname.NAME,
                new PersonNameAttributeValueBuilder().withValue(firstName).build());

        hubResponseAttributes.put(
                IdaConstants.Attributes_1_1.Middlename.NAME,
                new PersonNameAttributeValueBuilder().withValue(middleName).build());

        hubResponseAttributes.put(
                IdaConstants.Attributes_1_1.Surname.NAME,
                new PersonNameAttributeValueBuilder().withValue(surname).build());

        hubResponseAttributes.put(IdaConstants.Attributes_1_1.DateOfBirth.NAME,
                new DateAttributeValueBuilder().withValue(dob).build());

        return hubResponseAttributes;
    }

    private XMLObject extractAttribute(List<Attribute> attributes, String attributeName) {
        return attributes.stream()
                .filter(attribute -> attribute.getName().equals(attributeName))
                .findFirst().get()
                .getAttributeValues().get(0);
    }
}
