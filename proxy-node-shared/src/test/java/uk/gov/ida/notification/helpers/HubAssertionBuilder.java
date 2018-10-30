package uk.gov.ida.notification.helpers;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.signature.Signature;
import uk.gov.ida.saml.core.extensions.Address;
import uk.gov.ida.saml.core.test.builders.AddressAttributeBuilder_1_1;
import uk.gov.ida.saml.core.test.builders.AddressAttributeValueBuilder_1_1;
import uk.gov.ida.saml.core.test.builders.AssertionBuilder;
import uk.gov.ida.saml.core.test.builders.DateAttributeBuilder_1_1;
import uk.gov.ida.saml.core.test.builders.DateAttributeValueBuilder;
import uk.gov.ida.saml.core.test.builders.GenderAttributeBuilder_1_1;
import uk.gov.ida.saml.core.test.builders.PersonNameAttributeBuilder_1_1;
import uk.gov.ida.saml.core.test.builders.PersonNameAttributeValueBuilder;
import uk.gov.ida.saml.core.test.builders.SignatureBuilder;

import java.util.UUID;

import static uk.gov.ida.saml.core.test.builders.AttributeStatementBuilder.anAttributeStatement;
import static uk.gov.ida.saml.core.test.builders.AuthnStatementBuilder.anAuthnStatement;
import static uk.gov.ida.saml.core.test.builders.IPAddressAttributeBuilder.anIPAddress;
import static uk.gov.ida.saml.core.test.builders.IssuerBuilder.anIssuer;
import static uk.gov.ida.saml.core.test.builders.SubjectBuilder.aSubject;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;

public class HubAssertionBuilder {
    private final AssertionBuilder assertionBuilder;

    private HubAssertionBuilder() {
        this.assertionBuilder = AssertionBuilder
            .anAssertion()
            .withId(UUID.randomUUID().toString());
    }

    public static HubAssertionBuilder aMatchingDatasetAssertion() {
        return new HubAssertionBuilder()
            .addAttributeStatement(defaultMatchingDatasetAttributeStatement());
    }

    public static HubAssertionBuilder anAuthnStatementAssertion() {
        AttributeStatement authnAttributeStatement = anAttributeStatement()
            .addAttribute(anIPAddress().build())
            .build();
        return new HubAssertionBuilder()
            .addAuthnStatement(anAuthnStatement().build())
            .addAttributeStatement(authnAttributeStatement);
    }

    public HubAssertionBuilder addAuthnStatement(AuthnStatement authnStatement) {
        assertionBuilder.addAuthnStatement(authnStatement);
        return this;
    }

    public HubAssertionBuilder addAttributeStatement(AttributeStatement attributeStatement) {
        assertionBuilder.addAttributeStatement(attributeStatement);
        return this;
    }

    public HubAssertionBuilder withSignature(Credential signingCredential, String certificate) {
        Signature signature = SignatureBuilder
            .aSignature()
            .withSigningCredential(signingCredential)
            .withX509Data(certificate)
            .build();
        assertionBuilder.withSignature(signature);
        return this;
    }

    public HubAssertionBuilder withIssuer(String issuerId) {
        assertionBuilder.withIssuer(anIssuer().withIssuerId(issuerId).build());
        return this;
    }

    public HubAssertionBuilder withSubject(String recipient) {
        SubjectConfirmationData subjectConfirmationData = aSubjectConfirmationData()
            .withRecipient(recipient)
            .build();
        SubjectConfirmation subjectConfirmation = aSubjectConfirmation()
            .withSubjectConfirmationData(subjectConfirmationData)
            .build();
        Subject subject = aSubject()
            .withSubjectConfirmation(subjectConfirmation)
            .build();
        assertionBuilder.withSubject(subject);
        return this;
    }

    public EncryptedAssertion buildEncrypted(Credential encryptingCredential) {
        return assertionBuilder.buildWithEncrypterCredential(encryptingCredential);
    }

    public Assertion build() {
        return assertionBuilder.buildUnencrypted();
    }

    private static AttributeStatement defaultMatchingDatasetAttributeStatement() {
        AttributeValue firstnameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("Jazzy").withVerified(true).build();
        AttributeValue middlenameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("Harold").withVerified(true).build();
        AttributeValue surnameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("Jefferson").withVerified(true).build();
        AttributeValue dobValue = DateAttributeValueBuilder.aDateValue().withValue("1966-04-23").withVerified(true).build();
        Address currentAddressValue = AddressAttributeValueBuilder_1_1.anAddressAttributeValue().withPostcode("WC2 BNX").withVerified(true).build();
        Address previousAddressValue = AddressAttributeValueBuilder_1_1.anAddressAttributeValue().withPostcode("WC1 ANX").build();

        return anAttributeStatement()
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(firstnameValue).buildAsFirstname())
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(middlenameValue).buildAsMiddlename())
            .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(surnameValue).buildAsSurname())
            .addAttribute(GenderAttributeBuilder_1_1.aGender_1_1().withValue("male").build())
            .addAttribute(DateAttributeBuilder_1_1.aDate_1_1().addValue(dobValue).buildAsDateOfBirth())
            .addAttribute(AddressAttributeBuilder_1_1.anAddressAttribute().addAddress(currentAddressValue).buildCurrentAddress())
            .addAttribute(AddressAttributeBuilder_1_1.anAddressAttribute().addAddress(previousAddressValue).buildPreviousAddress())
            .build();
    }
}
