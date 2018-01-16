package uk.gov.ida.notification.apprule;

import org.glassfish.jersey.internal.util.Base64;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.security.credential.Credential;
import uk.gov.ida.notification.apprule.base.ProxyNodeAppRuleTestBase;
import uk.gov.ida.notification.helpers.HtmlHelpers;
import uk.gov.ida.notification.pki.KeyPairConfiguration;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;
import uk.gov.ida.notification.saml.SamlParser;
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

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;

import static org.junit.Assert.assertEquals;
import static uk.gov.ida.saml.core.test.builders.AttributeStatementBuilder.anAttributeStatement;

public class HubResponseAppRuleTests extends ProxyNodeAppRuleTestBase {
    private SamlObjectMarshaller marshaller = new SamlObjectMarshaller();
    private SamlParser parser;

    @Before
    public void setup() throws Throwable {
        parser = new SamlParser();
    }

    @Test
    public void postingHubResponseShouldReturnEidasResponseForm() throws Throwable {
        KeyPairConfiguration hubFacingEncryptionKeyPair = proxyNodeAppRule.getConfiguration().getHubFacingEncryptionKeyPair();
        Credential credential = new BasicCredential(
                hubFacingEncryptionKeyPair.getPublicKey().getPublicKey(),
                hubFacingEncryptionKeyPair.getPrivateKey().getPrivateKey()
        );
        Response hubResponse = ResponseBuilder.aResponse()
                .addEncryptedAssertion(anAuthnStatementAssertion().buildWithEncrypterCredential(credential))
                .addEncryptedAssertion(aMatchingDatasetAssertion().buildWithEncrypterCredential(credential))
                .build();
        String encodedResponse = Base64.encodeAsString(marshaller.transformToString(hubResponse));
        Form postForm = new Form().param(SamlFormMessageType.SAML_RESPONSE, encodedResponse);

        String html = proxyNodeAppRule.target("/SAML2/SSO/Response/POST").request()
                .post(Entity.form(postForm))
                .readEntity(String.class);

        String decodedEidasResponse = HtmlHelpers.getValueFromForm(html, "saml-form", SamlFormMessageType.SAML_RESPONSE);
        Response eidasResponse = parser.parseSamlString(decodedEidasResponse);

        assertEquals(hubResponse.getInResponseTo(), eidasResponse.getInResponseTo());
    }

    private static AssertionBuilder anAuthnStatementAssertion() {
        return AssertionBuilder.anAssertion()
                .addAuthnStatement(AuthnStatementBuilder.anAuthnStatement().build())
                .addAttributeStatement(anAttributeStatement().addAttribute(IPAddressAttributeBuilder.anIPAddress().build()).build());
    }

    private static AssertionBuilder aMatchingDatasetAssertion() {
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
}
