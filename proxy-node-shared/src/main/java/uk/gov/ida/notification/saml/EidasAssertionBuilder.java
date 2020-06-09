package uk.gov.ida.notification.saml;

import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;

import java.util.List;

public class EidasAssertionBuilder {

    private final Assertion assertion;

    public EidasAssertionBuilder() {
        assertion = SamlBuilder.build(Assertion.DEFAULT_ELEMENT_NAME);
    }

    public EidasAssertionBuilder withId(String id) {
        assertion.setID(id);
        return this;
    }

    public EidasAssertionBuilder withSubject(String pid, String requestId, String destination) {
        assertion.setSubject(createSubject(pid, requestId, destination));
        return this;
    }

    public EidasAssertionBuilder withIssuer(String issuerId) {
        assertion.setIssuer(createIssuer(issuerId));
        return this;
    }

    public EidasAssertionBuilder withIssueInstant(DateTime issueInstant) {
        assertion.setIssueInstant(issueInstant);
        return this;
    }

    public EidasAssertionBuilder withConditions(String audienceUri) {
        assertion.setConditions(createConditions(audienceUri));
        return this;
    }

    public EidasAssertionBuilder addAuthnStatement(String authnStatement, DateTime authnIssueInstant) {
        assertion.getAuthnStatements().add(createAuthnStatement(authnStatement, authnIssueInstant));
        return this;
    }

    public EidasAssertionBuilder addAttributeStatement(List<Attribute> attributes) {
        assertion.getAttributeStatements().add(createAttributeStatement(attributes));
        return this;
    }

    public Assertion build() {
        return assertion;
    }

    private Subject createSubject(String pid, String requestId, String destination) {
        Subject subject = SamlBuilder.build(Subject.DEFAULT_ELEMENT_NAME);
        NameID nameID = SamlBuilder.build(NameID.DEFAULT_ELEMENT_NAME);
        nameID.setValue(pid);
        nameID.setFormat(NameIDType.PERSISTENT);
        subject.setNameID(nameID);

        SubjectConfirmationData subjectConfirmationData = SamlBuilder.build(SubjectConfirmationData.DEFAULT_ELEMENT_NAME);
        subjectConfirmationData.setInResponseTo(requestId);
        subjectConfirmationData.setNotBefore(DateTime.now());
        subjectConfirmationData.setNotOnOrAfter(DateTime.now().plusMinutes(5));
        subjectConfirmationData.setRecipient(destination);

        SubjectConfirmation subjectConfirmation = SamlBuilder.build(SubjectConfirmation.DEFAULT_ELEMENT_NAME);
        subjectConfirmation.setMethod("urn:oasis:names:tc:saml2:2.0:cm:bearer");
        subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);

        subject.getSubjectConfirmations().add(subjectConfirmation);
        return subject;
    }

    private Conditions createConditions(String audienceUri) {
        Audience audience = SamlBuilder.build(Audience.DEFAULT_ELEMENT_NAME);
        audience.setAudienceURI(audienceUri);

        AudienceRestriction audienceRestriction = SamlBuilder.build(AudienceRestriction.DEFAULT_ELEMENT_NAME);
        audienceRestriction.getAudiences().add(audience);

        Conditions conditions = SamlBuilder.build(Conditions.DEFAULT_ELEMENT_NAME);
        DateTime now = DateTime.now();
        conditions.setNotBefore(now);
        conditions.setNotOnOrAfter(now.plusMinutes(5));
        conditions.getAudienceRestrictions().add(audienceRestriction);
        return conditions;
    }

    private AttributeStatement createAttributeStatement(List<Attribute> attributes) {
        AttributeStatement attributeStatement = SamlBuilder.build(AttributeStatement.DEFAULT_ELEMENT_NAME);
        attributeStatement.getAttributes().addAll(attributes);
        return attributeStatement;
    }

    private AuthnStatement createAuthnStatement(String authnStatementValue, DateTime authnStatementAuthnInstant) {
        AuthnStatement authnStatement = SamlBuilder.build(AuthnStatement.DEFAULT_ELEMENT_NAME);
        AuthnContext authnContext = SamlBuilder.build(AuthnContext.DEFAULT_ELEMENT_NAME);
        AuthnContextClassRef authnContextClassRef = SamlBuilder.build(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        authnContextClassRef.setAuthnContextClassRef(authnStatementValue);
        authnContext.setAuthnContextClassRef(authnContextClassRef);
        authnStatement.setAuthnContext(authnContext);
        authnStatement.setAuthnInstant(authnStatementAuthnInstant);
        return authnStatement;
    }

    private Issuer createIssuer(String issuerId) {
        Issuer issuer = SamlBuilder.build(Issuer.DEFAULT_ELEMENT_NAME);
        issuer.setFormat(NameIDType.ENTITY);
        issuer.setValue(issuerId);
        return issuer;
    }
}
