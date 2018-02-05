package uk.gov.ida.notification.saml.translation;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeValue;
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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class HubResponseTranslatorTest extends SamlInitializedTest {

    @Mock
    EidasResponseBuilder eidasResponseBuilder;

    @Captor
    private ArgumentCaptor<List<Attribute>> attributeListCaptor;

    @Test
    public void shouldTranslateVerifyTermsIntoEidasEquivalentsWhenGeneratingEidasResponse() {
        HubResponseTranslator hubResponseTranslator = new HubResponseTranslator(eidasResponseBuilder, "www.example.com/response/POST", "issuerId");
        DateTime dummyTime = DateTime.now();
        HubResponseContainer hubResponseContainer = new HubResponseContainer(
                new HubResponse("success", "response id", "id of request", dummyTime),
                new HubMdsAssertion(buildHubAttributes("Jane", "Doe","Smith", "1984-02-29"), dummyTime),
                new HubAuthnAssertion("pid", IdaAuthnContext.LEVEL_2_AUTHN_CTX, dummyTime)
        );

        hubResponseTranslator.translate(hubResponseContainer);

        verify(eidasResponseBuilder).createEidasResponse(
                eq("issuerId"), eq("success"), eq("pid"),
                eq(EidasConstants.EIDAS_LOA_SUBSTANTIAL), attributeListCaptor.capture(),
                eq("id of request"), eq(dummyTime),
                eq(dummyTime), eq(dummyTime), eq("www.example.com/response/POST"));
        assertEquals(4,
                attributeListCaptor.getValue().size());
        assertEquals("Jane Doe",
                ((CurrentGivenNameTypeImpl) extractAttribute(AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_NAME)).getValue());
        assertEquals("Smith",
                ((CurrentFamilyNameTypeImpl) extractAttribute(AttributeConstants.EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_NAME)).getValue());
        assertEquals(new LocalDate(1984, 2, 29),
                ((DateOfBirthTypeImpl) extractAttribute(AttributeConstants.EIDAS_DATE_OF_BIRTH_ATTRIBUTE_NAME)).getDate());
        assertEquals(EidasResponseBuilder.TEMPORARY_PID_TRANSLATION + "pid",
                ((PersonIdentifierTypeImpl) extractAttribute(AttributeConstants.EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_NAME)).getValue());
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

    private XMLObject extractAttribute(String attributeName) {
        return attributeListCaptor.getValue().stream()
                .filter(attribute -> attribute.getName().equals(attributeName))
                .findFirst().get()
                .getAttributeValues().get(0);
    }
}
