package uk.gov.ida.notification.saml;

import org.junit.Test;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.notification.SamlInitializedTest;
import uk.gov.ida.notification.helpers.TestKeyPair;
import uk.gov.ida.notification.pki.DecryptingCredential;
import uk.gov.ida.saml.core.test.builders.AssertionBuilder;
import uk.gov.ida.saml.core.test.builders.ResponseBuilder;

import static org.junit.Assert.assertEquals;

public class ResponseAssertionDecrypterTest extends SamlInitializedTest {
    @Test
    public void shouldDecryptAssertionsInResponse() throws Throwable {
        TestKeyPair testKeyPair = new TestKeyPair();
        DecryptingCredential credential = new DecryptingCredential(testKeyPair.getPublicKey(), testKeyPair.getPrivateKey());

        EncryptedAssertion assertion = AssertionBuilder.anAssertion().withId("hi").buildWithEncrypterCredential(credential);
        Response encryptedResponse = ResponseBuilder.aResponse().addEncryptedAssertion(assertion).build();

        ResponseAssertionDecrypter decrypter = new ResponseAssertionDecrypter(credential);
        Response decryptedResponse = decrypter.decrypt(encryptedResponse);

        assertEquals("hi", decryptedResponse.getAssertions().get(0).getID());
        assertEquals(0, decryptedResponse.getEncryptedAssertions().size());
    }
}