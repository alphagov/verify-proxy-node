package uk.gov.ida.notification.saml;

import net.shibboleth.utilities.java.support.security.SecureRandomIdentifierGenerationStrategy;
import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;

import java.util.List;

public class EidasResponseBuilder {

    private static final SecureRandomIdentifierGenerationStrategy idGeneratorStrategy = new SecureRandomIdentifierGenerationStrategy();
    private Response eidasResponse;
    private EidasAssertionBuilder assertionBuilder;

    private EidasResponseBuilder() {
        eidasResponse = SamlBuilder.build(Response.DEFAULT_ELEMENT_NAME);
        assertionBuilder = new EidasAssertionBuilder();
        withId(generateRandomId());
        assertionBuilder.withId(generateRandomId());
    }

    public static EidasResponseBuilder instance() {
        return new EidasResponseBuilder();
    }

    public EidasResponseBuilder withId(String id) {
        eidasResponse.setID(id);
        return this;
    }

    public EidasResponseBuilder withIssuer(String issuerId) {
        assertionBuilder.withIssuer(issuerId);
        eidasResponse.setIssuer(createIssuer(issuerId));
        return this;
    }

    public EidasResponseBuilder withStatus(String statusCodeValue) {
        eidasResponse.setStatus(createStatus(statusCodeValue));
        return this;
    }

    public EidasResponseBuilder withInResponseTo(String inResponseTo) {
        eidasResponse.setInResponseTo(inResponseTo);
        return this;
    }

    public EidasResponseBuilder withDestination(String destination) {
        eidasResponse.setDestination(destination);
        return this;
    }

    public EidasResponseBuilder withIssueInstant(DateTime issueInstant) {
        assertionBuilder.withIssueInstant(issueInstant);
        eidasResponse.setIssueInstant(issueInstant);
        return this;
    }

    public EidasResponseBuilder withAssertionSubject(String pid, String requestId, String destination) {
        assertionBuilder.withSubject(pid, requestId, destination);
        return this;
    }

    public EidasResponseBuilder addAssertionAttributeStatement(List<Attribute> attributes) {
        assertionBuilder.addAttributeStatement(attributes);
        return this;
    }

    public EidasResponseBuilder addAssertionAuthnStatement(String authnStatement, DateTime authnIssueInstant) {
        assertionBuilder.addAuthnStatement(authnStatement, authnIssueInstant);
        return this;
    }

    public EidasResponseBuilder withLoa(String loa, DateTime authnIssueInstant) {
        return addAssertionAuthnStatement(loa, authnIssueInstant);
    }

    public EidasResponseBuilder withAssertionConditions(String assertionConditions) {
        assertionBuilder.withConditions(assertionConditions);
        return this;
    }

    public Response build() {
        eidasResponse.getAssertions().add(assertionBuilder.build());
        return eidasResponse;
    }

    private static String generateRandomId(){
        return idGeneratorStrategy.generateIdentifier(true);
    }

    private Status createStatus(String statusCodeValue) {
        Status status = SamlBuilder.build(Status.DEFAULT_ELEMENT_NAME);
        StatusCode statusCode = SamlBuilder.build(StatusCode.DEFAULT_ELEMENT_NAME);
        statusCode.setValue(statusCodeValue);
        status.setStatusCode(statusCode);
        return status;
    }

    private Issuer createIssuer(String issuerId) {
        Issuer issuer = SamlBuilder.build(Issuer.DEFAULT_ELEMENT_NAME);
        issuer.setFormat(NameIDType.ENTITY);
        issuer.setValue(issuerId);
        return issuer;
    }
}
