package uk.gov.ida.notification.saml.translation;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;
import org.opensaml.core.xml.AbstractXMLObject;
import org.opensaml.core.xml.schema.impl.XSStringImpl;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.Response;
import se.litsec.eidas.opensaml.ext.attributes.AttributeConstants;
import se.litsec.eidas.opensaml.ext.attributes.impl.DateOfBirthTypeImpl;
import uk.gov.ida.notification.SamlInitializedTest;
import uk.gov.ida.notification.saml.SamlParser;
import uk.gov.ida.saml.core.IdaConstants;
import uk.gov.ida.saml.core.extensions.IdaAuthnContext;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class HubResponseTranslatorTest extends SamlInitializedTest {
    @Test
    public void shouldGenerateEidasResponse() throws Throwable {
        SamlParser samlParser = new SamlParser();
        HubResponseTranslator hubResponseTranslator = new HubResponseTranslator("http://proxy-node.uk", "http://connector.eu");
        HubResponse hubResponse = new HubResponse(
            "pid",
            "success",
            IdaAuthnContext.LEVEL_2_AUTHN_CTX,
            "response id",
            "id of request",
            buildHubAttributes("Jane", "Smith", "1984-02-29")
        );

        Response eidasResponse = hubResponseTranslator.translate(hubResponse);
        Map<String, AbstractXMLObject> eidasResponseAttributes = getEidasResponseAttributes(eidasResponse);

        Assert.assertEquals("id of request", eidasResponse.getInResponseTo());
        Assert.assertEquals("Jane", ((XSStringImpl) eidasResponseAttributes.get(AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_NAME)).getValue());
        Assert.assertEquals("Smith", ((XSStringImpl) eidasResponseAttributes.get(AttributeConstants.EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_NAME)).getValue());
        Assert.assertEquals(new LocalDate(1984, 2, 29), ((DateOfBirthTypeImpl) eidasResponseAttributes.get(AttributeConstants.EIDAS_DATE_OF_BIRTH_ATTRIBUTE_NAME)).getDate());
    }

    private Map<String, AbstractXMLObject> getEidasResponseAttributes(Response eidasResponse) {
        return eidasResponse.getAssertions().get(0).getAttributeStatements().get(0).getAttributes()
            .stream()
            .collect(Collectors.toMap(
                Attribute::getName,
                a -> (AbstractXMLObject) a.getAttributeValues().get(0)));
    }

    private Map<String, String> buildHubAttributes(String firstName, String surName, String dob) {
        Map<String, String> hubResponseAttributes = new HashMap<>();
        hubResponseAttributes.put(IdaConstants.Attributes_1_1.Firstname.NAME, firstName);
        hubResponseAttributes.put(IdaConstants.Attributes_1_1.Surname.NAME, surName);
        hubResponseAttributes.put(IdaConstants.Attributes_1_1.DateOfBirth.NAME, dob);
        return hubResponseAttributes;
    }
}
