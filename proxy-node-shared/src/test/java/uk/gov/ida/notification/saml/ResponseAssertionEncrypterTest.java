package uk.gov.ida.notification.saml;

import org.junit.Test;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.encryption.Decrypter;
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.security.x509.X509Credential;
import uk.gov.ida.notification.SamlInitializedTest;
import uk.gov.ida.notification.helpers.TestKeyPair;
import uk.gov.ida.saml.core.test.builders.AssertionBuilder;
import uk.gov.ida.saml.core.test.builders.ResponseBuilder;
import uk.gov.ida.saml.security.DecrypterFactory;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class ResponseAssertionEncrypterTest extends SamlInitializedTest {
    @Test
    public void shouldEncryptAssertionsInResponse() throws Throwable {
        TestKeyPair testKeyPair = new TestKeyPair();
        BasicCredential decryptionCredential = new BasicCredential(testKeyPair.publicKey, testKeyPair.privateKey);
        X509Credential credential = new BasicX509Credential(testKeyPair.certificate);

        Assertion assertion = AssertionBuilder.anAssertion().withId("hi").buildUnencrypted();
        Response response = ResponseBuilder.aResponse().addAssertion(assertion).build();

        ResponseAssertionEncrypter encrypter = new ResponseAssertionEncrypter(credential);
        Response encryptedResponse = encrypter.encrypt(response);

        EncryptedAssertion encryptedAssertion = encryptedResponse.getEncryptedAssertions().get(0);

        assertThat("hi").isEqualTo(decryptAssertion(encryptedAssertion, decryptionCredential).getID());
        assertThat(1).isEqualTo(encryptedResponse.getEncryptedAssertions().size());
        assertThat(0).isEqualTo(encryptedResponse.getAssertions().size());
    }

    private static Assertion decryptAssertion(EncryptedAssertion encryptedAssertion, Credential credential) throws Exception {
        DecrypterFactory decrypterFactory = new DecrypterFactory();
        Decrypter decrypter = decrypterFactory.createDecrypter(Collections.singletonList(credential));
        return decrypter.decrypt(encryptedAssertion);
    }
}
