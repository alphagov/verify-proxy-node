package uk.gov.ida.notification.saml;

import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.junit.Test;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Condition;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.impl.AudienceRestrictionImpl;
import se.litsec.eidas.opensaml.ext.attributes.CurrentGivenNameType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class EidasResponseBuilderTest {

    static {
        try {
            InitializationService.initialize();
        } catch (InitializationException e) {
            throw new IllegalStateException("Could not initialize opensaml in test", e);
        }
    }

    @Test
    public void testAnEidasResponseHasASeparateIdToItsAssertionId() {
        Response response = EidasResponseBuilder.instance().build();
        assertNotEquals(response.getAssertions().iterator().next().getID(), response.getID());
    }

    @Test
    public void testAnEidasResponseHasSameIssuerValueAsItsAssertionIssuerValue() {
        Response response = EidasResponseBuilder.instance().withIssuer("issuer").build();
        assertEquals(response.getAssertions().iterator().next().getIssuer().getValue(), response.getIssuer().getValue());
    }

    @Test
    public void testAnEidasResponseHasSuppliedStatusAsObject() {
        Response response = EidasResponseBuilder.instance().withStatus("status").build();
        Status status = response.getStatus();
        assertThat(status.getStatusCode().getValue()).isEqualTo("status");
    }

    @Test
    public void testAnEidasResponseHasSuppliedDestination() {
        Response response = EidasResponseBuilder.instance().withDestination("a destination").build();
        String destination = response.getDestination();
        assertThat(destination).isEqualTo("a destination");
    }

    @Test
    public void testAnEidasResponseHasSuppliedInResponseTo() {
        Response response = EidasResponseBuilder.instance().withInResponseTo("in response to reference").build();
        assertThat(response.getInResponseTo()).isEqualTo("in response to reference");
    }

    @Test
    public void testAnEidasResponseAndAssertionHaveSameSuppliedIssueInstant() {
        Response response = EidasResponseBuilder.instance().withIssueInstant(DateTime.now()).build();
        assertThat(response.getIssueInstant()).isEqualByComparingTo(response.getAssertions().iterator().next().getIssueInstant());
    }

    @Test
    public void testAllResponseAssertionsAreSet() {

        Attribute attribute = new EidasAttributeBuilder(
                "a name",
                "a friendly name",
                CurrentGivenNameType.TYPE_NAME, "name")
                .build();

        DateTime now = DateTime.now();
        Response response = EidasResponseBuilder.instance()
                .withAssertionSubject("an assertion subject")
                .withAssertionConditions("some assertion conditions")
                .addAssertionAttributeStatement(Lists.newArrayList(attribute))
                .addAssertionAuthnStatement("an authStatement", now)
                .build();


        List<Assertion> assertions = response.getAssertions();
        assertThat(assertions.size()).isEqualTo(1);

        Assertion assertion = assertions.iterator().next();

        assertThat(assertion.getSubject().getNameID().getValue()).contains("an assertion subject");

        Conditions conditions = assertion.getConditions();
        assertThat(conditions.getConditions().size()).isEqualTo(1);
        Condition condition = conditions.getConditions().iterator().next();
        assertThat(((AudienceRestrictionImpl) condition).getAudiences().iterator().next().getAudienceURI()).isEqualTo("some assertion conditions");

        List<AttributeStatement> attributeStatements = assertion.getAttributeStatements();
        assertThat(attributeStatements.size()).isEqualTo(1);
        AttributeStatement attributeStatement = attributeStatements.iterator().next();
        List<Attribute> attributes = attributeStatement.getAttributes();
        assertThat(attributes.size()).isEqualTo(1);
        assertThat(attributeStatement.getAttributes().iterator().next()).isEqualTo(attribute);

        assertThat(assertion.getAuthnStatements().size()).isEqualTo(1);
        AuthnStatement authnStatement = assertion.getAuthnStatements().iterator().next();
        assertThat(authnStatement.getAuthnInstant()).isEqualByComparingTo(now);
        assertThat(authnStatement.getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef()).isEqualTo("an authStatement");
    }
}