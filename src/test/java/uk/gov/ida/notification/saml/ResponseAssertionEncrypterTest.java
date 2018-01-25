package uk.gov.ida.notification.saml;

import org.junit.Test;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.encryption.Decrypter;
import uk.gov.ida.notification.SamlInitializedTest;
import uk.gov.ida.notification.helpers.TestKeyPair;
import uk.gov.ida.notification.pki.DecryptionCredential;
import uk.gov.ida.notification.pki.EncryptionCredential;
import uk.gov.ida.saml.core.test.builders.AssertionBuilder;
import uk.gov.ida.saml.core.test.builders.ResponseBuilder;
import uk.gov.ida.saml.security.DecrypterFactory;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class ResponseAssertionEncrypterTest extends SamlInitializedTest {
    @Test
    public void shouldEncryptAssertionsInResponse() throws Throwable {
        TestKeyPair testKeyPair = new TestKeyPair();
        DecryptionCredential decryptionCredential = new DecryptionCredential(testKeyPair.publicKey, testKeyPair.privateKey);
        EncryptionCredential encryptionCredential = new EncryptionCredential(testKeyPair.publicKey);

        Assertion assertion = AssertionBuilder.anAssertion().withId("hi").buildUnencrypted();
        Response response = ResponseBuilder.aResponse().addAssertion(assertion).build();

        ResponseAssertionEncrypter encrypter = new ResponseAssertionEncrypter(encryptionCredential);
        Response encryptedResponse = encrypter.encrypt(response);

        EncryptedAssertion encryptedAssertion = encryptedResponse.getEncryptedAssertions().get(0);

        assertEquals("hi", decryptAssertion(encryptedAssertion, decryptionCredential).getID());
        assertEquals(1, encryptedResponse.getEncryptedAssertions().size());
        assertEquals(0, encryptedResponse.getAssertions().size());
    }

    private static Assertion decryptAssertion(EncryptedAssertion encryptedAssertion, DecryptionCredential credential) throws Exception {
        DecrypterFactory decrypterFactory = new DecrypterFactory();
        Decrypter decrypter = decrypterFactory.createDecrypter(Collections.singletonList(credential));
        return decrypter.decrypt(encryptedAssertion);
    }
}
