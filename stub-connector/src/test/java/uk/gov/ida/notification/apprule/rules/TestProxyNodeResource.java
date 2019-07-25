package uk.gov.ida.notification.apprule.rules;

import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.credential.Credential;
import uk.gov.ida.notification.helpers.EidasAuthnRequestBuilder;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.builders.AssertionBuilder;
import uk.gov.ida.saml.core.test.builders.AttributeStatementBuilder;
import uk.gov.ida.saml.core.test.builders.AuthnStatementBuilder;
import uk.gov.ida.saml.core.test.builders.PersonNameAttributeBuilder_1_1;
import uk.gov.ida.saml.core.test.builders.PersonNameAttributeValueBuilder;
import uk.gov.ida.saml.core.test.builders.ResponseBuilder;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_ENCRYPTION_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_ENCRYPTION_CERT;

@Path("/translator")
public class TestProxyNodeResource {

    @POST
    @Path("/SAML2/SSO/Response")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_XML)
    public String hubResponse(@FormParam(SamlFormMessageType.SAML_RESPONSE) String encryptedHubResponse) throws Throwable {
        Credential encryptingCredential = new TestCredentialFactory(
                TEST_RP_PUBLIC_ENCRYPTION_CERT,
                TEST_RP_PRIVATE_ENCRYPTION_KEY
        ).getDecryptingCredential();

        AttributeValue firstNameValue = PersonNameAttributeValueBuilder
                .aPersonNameValue()
                .withValue("Jazzy Harold")
                .withVerified(true)
                .build();

        Attribute firstName = PersonNameAttributeBuilder_1_1.aPersonName_1_1()
                .addValue(firstNameValue)
                .buildAsFirstname();

        AttributeStatement attributeStatement = AttributeStatementBuilder
                .anAttributeStatement()
                .addAttribute(firstName)
                .build();

        AuthnStatement authnStatement = AuthnStatementBuilder.anAuthnStatement().build();

        EncryptedAssertion encryptedAssertion = AssertionBuilder
                .anAssertion()
                .withId("encryptedAssertion")
                .addAttributeStatement(attributeStatement)
                .addAuthnStatement(authnStatement)
                .buildWithEncrypterCredential(encryptingCredential);

        Response response = ResponseBuilder
                .aResponse()
                .withInResponseTo(new EidasAuthnRequestBuilder().build().getID())
                .addEncryptedAssertion(encryptedAssertion)
                .build();

        SamlObjectMarshaller marshaller = new SamlObjectMarshaller();
        return marshaller.transformToString(response);
    }
}
