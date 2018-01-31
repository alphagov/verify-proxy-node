package uk.gov.ida.notification.saml.translation;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.opensaml.core.xml.AbstractXMLObject;
import org.opensaml.core.xml.schema.impl.XSStringImpl;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.Response;
import se.litsec.eidas.opensaml.ext.attributes.AttributeConstants;
import se.litsec.eidas.opensaml.ext.attributes.impl.DateOfBirthTypeImpl;
import uk.gov.ida.notification.SamlInitializedTest;
import uk.gov.ida.saml.core.IdaConstants;
import uk.gov.ida.saml.core.extensions.IdaAuthnContext;
import uk.gov.ida.saml.core.test.builders.DateAttributeValueBuilder;
import uk.gov.ida.saml.core.test.builders.PersonNameAttributeValueBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HubResponseTranslatorTest extends SamlInitializedTest {
    @Test
    public void shouldGenerateEidasResponse() throws Exception {
        String proxyNodeMetadataForConnectorNodeUrl = "http://proxy-node.uk/connector-node-metadata";
        String connectorNodeIssuerId = "connectorNode issuerId";
        HubResponseTranslator hubResponseTranslator = new HubResponseTranslator(new EidasResponseBuilder("http://connector.eu", proxyNodeMetadataForConnectorNodeUrl, connectorNodeIssuerId));
        DateTime dummyTime = DateTime.now();
        HubResponseContainer hubResponseContainer = new HubResponseContainer(
                new HubResponse("success", "response id", "id of request", dummyTime),
                new HubMdsAssertion(buildHubAttributes("Jane", "Smith", "1984-02-29"), dummyTime),
                new HubAuthnAssertion("pid", IdaAuthnContext.LEVEL_2_AUTHN_CTX, dummyTime)
        );

        Response eidasResponse = hubResponseTranslator.translate(hubResponseContainer);
        Map<String, AbstractXMLObject> eidasResponseAttributes = getEidasResponseAttributes(eidasResponse);

        Assertion authnAssertion = eidasResponse.getAssertions()
                .stream()
                .filter(a -> !a.getAuthnStatements().isEmpty())
                .findFirst()
                .orElseThrow(() -> new Exception("Hub Response has no authn assertion"));

        assertEquals("id of request", eidasResponse.getInResponseTo());
        assertEquals("Jane", ((XSStringImpl) eidasResponseAttributes.get(AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_NAME)).getValue());
        assertEquals("Smith", ((XSStringImpl) eidasResponseAttributes.get(AttributeConstants.EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_NAME)).getValue());
        assertEquals(new LocalDate(1984, 2, 29), ((
                DateOfBirthTypeImpl) eidasResponseAttributes.get(AttributeConstants.EIDAS_DATE_OF_BIRTH_ATTRIBUTE_NAME)).getDate());
        assertTrue(dummyTime.isEqual(eidasResponse.getIssueInstant()));
        assertTrue(dummyTime.isEqual(eidasResponse.getAssertions().get(0).getIssueInstant()));
        assertTrue(dummyTime.isEqual(authnAssertion.getAuthnStatements().get(0).getAuthnInstant()));
        assertEquals(proxyNodeMetadataForConnectorNodeUrl, eidasResponse.getIssuer().getValue());
        assertEquals(connectorNodeIssuerId, eidasResponse.getAssertions().get(0).getConditions().getAudienceRestrictions().get(0).getAudiences().get(0).getAudienceURI());

        assertEquals("UK/NL/pid", eidasResponse.getAssertions().get(0).getSubject().getNameID().getValue());
        assertEquals("UK/NL/pid", ((XSStringImpl) eidasResponseAttributes.get(AttributeConstants.EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_NAME)).getValue());
    }

    private Map<String, AbstractXMLObject> getEidasResponseAttributes(Response eidasResponse) {
        return eidasResponse.getAssertions().get(0).getAttributeStatements().get(0).getAttributes()
            .stream()
            .collect(Collectors.toMap(
                Attribute::getName,
                a -> (AbstractXMLObject) a.getAttributeValues().get(0)));
    }

    private Map<String, AttributeValue> buildHubAttributes(String firstname, String surname, String dob) {
        Map<String, AttributeValue> hubResponseAttributes = new HashMap<>();

        hubResponseAttributes.put(
                IdaConstants.Attributes_1_1.Firstname.NAME,
                new PersonNameAttributeValueBuilder().withValue(firstname).build());

        hubResponseAttributes.put(
                IdaConstants.Attributes_1_1.Surname.NAME,
                new PersonNameAttributeValueBuilder().withValue(surname).build());

        hubResponseAttributes.put(IdaConstants.Attributes_1_1.DateOfBirth.NAME,
                new DateAttributeValueBuilder().withValue(dob).build());

        return hubResponseAttributes;
    }
}
