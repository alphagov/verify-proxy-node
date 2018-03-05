package uk.gov.ida.notification.helpers;


import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.signature.support.SignatureException;
import uk.gov.ida.saml.core.extensions.Address;
import uk.gov.ida.saml.core.test.builders.AddressAttributeBuilder_1_1;
import uk.gov.ida.saml.core.test.builders.AddressAttributeValueBuilder_1_1;
import uk.gov.ida.saml.core.test.builders.AssertionBuilder;
import uk.gov.ida.saml.core.test.builders.AttributeStatementBuilder;
import uk.gov.ida.saml.core.test.builders.AuthnStatementBuilder;
import uk.gov.ida.saml.core.test.builders.DateAttributeBuilder_1_1;
import uk.gov.ida.saml.core.test.builders.DateAttributeValueBuilder;
import uk.gov.ida.saml.core.test.builders.GenderAttributeBuilder_1_1;
import uk.gov.ida.saml.core.test.builders.IPAddressAttributeBuilder;
import uk.gov.ida.saml.core.test.builders.PersonNameAttributeBuilder_1_1;
import uk.gov.ida.saml.core.test.builders.PersonNameAttributeValueBuilder;
import uk.gov.ida.saml.core.test.builders.ResponseBuilder;

import static uk.gov.ida.saml.core.test.builders.AttributeStatementBuilder.anAttributeStatement;
import static uk.gov.ida.saml.core.test.builders.IssuerBuilder.anIssuer;

public class HubResponseBuilder {

    private ResponseBuilder responseBuilder;

    public HubResponseBuilder() {
        responseBuilder = new ResponseBuilder();
    }

    public HubResponseBuilder addAssertion(Assertion assertion) {
        responseBuilder.addAssertion(assertion);
        return this;
    }

    public HubResponseBuilder addEncryptedAuthnStatementAssertionUsing(Credential credential) {
        responseBuilder.addEncryptedAssertion(anAuthnStatementAssertion().buildWithEncrypterCredential(credential));
        return this;
    }

    public HubResponseBuilder addEncryptedMatchingDatasetAssertionUsing(Credential credential) {
        responseBuilder.addEncryptedAssertion(aMatchingDatasetAssertion().buildWithEncrypterCredential(credential));
        return this;
    }

    public HubResponseBuilder withIssuer(String issuer) {
        responseBuilder.withIssuer(anIssuer().withIssuerId(issuer).build());
        return this;
    }

    public Response build() throws MarshallingException, SignatureException {
        return responseBuilder
                .withoutSignatureElement()
                .withoutSigning()
                .build();
    }

    public static AssertionBuilder aMatchingDatasetAssertion() {
        AttributeValue firstnameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("Jazzy").build();
        AttributeValue middlenameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("Harold").build();
        AttributeValue surnameValue = PersonNameAttributeValueBuilder.aPersonNameValue().withValue("Jefferson").build();
        AttributeValue dobValue = DateAttributeValueBuilder.aDateValue().withValue("1966-04-23").build();
        Address currentAddressValue = AddressAttributeValueBuilder_1_1.anAddressAttributeValue().withPostcode("WC2 BNX").build();
        Address previousAddressValue = AddressAttributeValueBuilder_1_1.anAddressAttributeValue().withPostcode("WC1 ANX").build();

        AttributeStatementBuilder attributeStatementBuilder = anAttributeStatement()
                .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(firstnameValue).buildAsFirstname())
                .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(middlenameValue).buildAsMiddlename())
                .addAttribute(PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(surnameValue).buildAsSurname())
                .addAttribute(GenderAttributeBuilder_1_1.aGender_1_1().withValue("male").build())
                .addAttribute(DateAttributeBuilder_1_1.aDate_1_1().addValue(dobValue).buildAsDateOfBirth())
                .addAttribute(AddressAttributeBuilder_1_1.anAddressAttribute().addAddress(currentAddressValue).buildCurrentAddress())
                .addAttribute(AddressAttributeBuilder_1_1.anAddressAttribute().addAddress(previousAddressValue).buildPreviousAddress());

        return AssertionBuilder.anAssertion()
                .addAttributeStatement(attributeStatementBuilder.build());
    }

    public static AssertionBuilder anAuthnStatementAssertion() {
        return AssertionBuilder.anAssertion()
                .addAuthnStatement(AuthnStatementBuilder.anAuthnStatement().build())
                .addAttributeStatement(anAttributeStatement().addAttribute(IPAddressAttributeBuilder.anIPAddress().build()).build());
    }

}
