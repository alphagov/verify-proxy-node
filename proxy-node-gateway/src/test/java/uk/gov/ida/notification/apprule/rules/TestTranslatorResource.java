package uk.gov.ida.notification.apprule.rules;

import org.opensaml.saml.saml2.core.*;
import org.opensaml.security.credential.Credential;
import uk.gov.ida.notification.helpers.EidasAuthnRequestBuilder;
import uk.gov.ida.notification.saml.SamlFormMessageType;
import uk.gov.ida.notification.saml.SamlObjectMarshaller;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.builders.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path(Urls.TranslatorUrls.TRANSLATOR_ROOT)
public class TestTranslatorResource {

    @POST
    @Path("Urls.TranslatorUrls.TRANSLATE_HUB_RESPONSE_PATH")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_XML)
    public String hubResponse(@FormParam(SamlFormMessageType.SAML_RESPONSE) String encryptedHubResponse) throws Throwable {
        Credential encryptingCredential = new TestCredentialFactory(
                TEST_RP_PUBLIC_ENCRYPTION_CERT,
                TEST_RP_PRIVATE_ENCRYPTION_KEY
        ).getDecryptingCredential();

        AttributeValue firstnameValue = PersonNameAttributeValueBuilder
                .aPersonNameValue()
                .withValue("Jazzy Harold")
                .withVerified(true)
                .build();

        Attribute firstname = PersonNameAttributeBuilder_1_1.aPersonName_1_1()
                .addValue(firstnameValue)
                .buildAsFirstname();

        AttributeStatement attributeStatement = AttributeStatementBuilder
                .anAttributeStatement()
                .addAttribute(firstname)
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
        String samlMessage = marshaller.transformToString(response);
        return samlMessage;
    }
}
