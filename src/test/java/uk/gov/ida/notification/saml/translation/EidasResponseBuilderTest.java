package uk.gov.ida.notification.saml.translation;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.core.xml.AbstractXMLObject;
import org.opensaml.core.xml.schema.impl.XSStringImpl;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.Response;
import se.litsec.eidas.opensaml.common.EidasConstants;
import se.litsec.eidas.opensaml.ext.attributes.AttributeConstants;
import se.litsec.eidas.opensaml.ext.attributes.CurrentFamilyNameType;
import se.litsec.eidas.opensaml.ext.attributes.CurrentGivenNameType;
import se.litsec.eidas.opensaml.ext.attributes.DateOfBirthType;
import se.litsec.eidas.opensaml.ext.attributes.PersonIdentifierType;
import se.litsec.eidas.opensaml.ext.attributes.impl.DateOfBirthTypeImpl;
import uk.gov.ida.notification.SamlInitializedTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class EidasResponseBuilderTest extends SamlInitializedTest {

    private String connectorNodeIssuerId = "connectorNode issuerId";
    private EidasResponseBuilder eidasResponseBuilder;

    @Before
    public void setUp() {
        eidasResponseBuilder = new EidasResponseBuilder(connectorNodeIssuerId);
    }

    @Test
    public void shouldGenerateAnEidasResponse() throws Exception {
        DateTime issueInstant = DateTime.now();
        List<Attribute> eidasAttributes = getEidasAttributes();

        Response response = eidasResponseBuilder.createEidasResponse("proxyNodeMetadataForConnectorNodeUrl", "success", "pid",
                EidasConstants.EIDAS_LOA_SUBSTANTIAL, eidasAttributes,"id-of-request", issueInstant, issueInstant, issueInstant, "http://connector.eu");
        Map<String, AbstractXMLObject> eidasResponseAttributes = getEidasResponseAttributes(response);
        Assertion authnAssertion = response.getAssertions()
                .stream()
                .filter(a -> !a.getAuthnStatements().isEmpty())
                .findFirst()
                .orElseThrow(() -> new Exception("Response has no authn assertion"));

        assertEquals("success", response.getStatus().getStatusCode().getValue());
        assertEquals("id-of-request",response.getInResponseTo());
        assertEquals("Bob", ((XSStringImpl) eidasResponseAttributes.get(AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_NAME)).getValue());
        assertEquals("Hobbs", ((XSStringImpl) eidasResponseAttributes.get(AttributeConstants.EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_NAME)).getValue());
        assertEquals(new LocalDate(1985, 1, 30), ((DateOfBirthTypeImpl)eidasResponseAttributes.get(AttributeConstants.EIDAS_DATE_OF_BIRTH_ATTRIBUTE_NAME)).getDate());
        assertEquals("UK/EU/pid", ((XSStringImpl) eidasResponseAttributes.get(AttributeConstants.EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_NAME)).getValue());
        assertTrue(issueInstant.isEqual(response.getIssueInstant()));
        assertTrue(issueInstant.isEqual(response.getAssertions().get(0).getIssueInstant()));
        assertTrue(issueInstant.isEqual(authnAssertion.getAuthnStatements().get(0).getAuthnInstant()));
        assertEquals("proxyNodeMetadataForConnectorNodeUrl", response.getIssuer().getValue());
        assertEquals(connectorNodeIssuerId, response.getAssertions().get(0).getConditions().getAudienceRestrictions().get(0).getAudiences().get(0).getAudienceURI());
        assertEquals("UK/EU/pid", response.getAssertions().get(0).getSubject().getNameID().getValue());
    }

    private Map<String, AbstractXMLObject> getEidasResponseAttributes(Response eidasResponse) {
        return eidasResponse.getAssertions().get(0).getAttributeStatements().get(0).getAttributes()
                .stream()
                .collect(Collectors.toMap(
                        Attribute::getName,
                        a -> (AbstractXMLObject) a.getAttributeValues().get(0)));
    }

    private List<Attribute> getEidasAttributes(){
        List<EidasAttributeBuilder> eidasAttributeBuilders = new ArrayList<>();
        eidasAttributeBuilders.add(new EidasAttributeBuilder(AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_NAME, AttributeConstants.EIDAS_CURRENT_GIVEN_NAME_ATTRIBUTE_FRIENDLY_NAME, CurrentFamilyNameType.TYPE_NAME ,"Bob"));
        eidasAttributeBuilders.add(new EidasAttributeBuilder(AttributeConstants.EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_NAME, AttributeConstants.EIDAS_CURRENT_FAMILY_NAME_ATTRIBUTE_FRIENDLY_NAME, CurrentGivenNameType.TYPE_NAME ,"Hobbs"));
        eidasAttributeBuilders.add(new EidasAttributeBuilder(AttributeConstants.EIDAS_DATE_OF_BIRTH_ATTRIBUTE_NAME, AttributeConstants.EIDAS_DATE_OF_BIRTH_ATTRIBUTE_FRIENDLY_NAME, DateOfBirthType.TYPE_NAME ,"1985-01-30"));
        eidasAttributeBuilders.add(new EidasAttributeBuilder(AttributeConstants.EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_NAME, AttributeConstants.EIDAS_PERSON_IDENTIFIER_ATTRIBUTE_FRIENDLY_NAME, PersonIdentifierType.TYPE_NAME ,"UK/EU/pid"));

        return eidasAttributeBuilders.stream()
                .map(builder -> builder.build())
                .collect(Collectors.toList());
    }
}

