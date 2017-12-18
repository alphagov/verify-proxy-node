package uk.gov.ida.notification.saml;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.encryption.Decrypter;
import org.opensaml.xmlsec.encryption.support.DecryptionException;
import uk.gov.ida.notification.exceptions.ResponseAssertionDecryptionException;
import uk.gov.ida.notification.pki.DecryptingCredential;
import uk.gov.ida.saml.security.DecrypterFactory;

import java.util.Collections;
import java.util.stream.Collectors;

public class ResponseAssertionDecrypter {
    private final Decrypter decrypter;

    public ResponseAssertionDecrypter(DecryptingCredential credential) {
        DecrypterFactory decrypterFactory = new DecrypterFactory();
        decrypter = decrypterFactory.createDecrypter(Collections.singletonList(credential));
        decrypter.setRootInNewDocument(true);
    }

    public Response decrypt(Response encryptedResponse) {
        encryptedResponse.getAssertions().clear();
        encryptedResponse.getAssertions().addAll(encryptedResponse.getEncryptedAssertions()
            .stream()
            .map(a -> decryptAssertion(a))
            .collect(Collectors.toList())
        );
        encryptedResponse.getEncryptedAssertions().clear();

        return encryptedResponse;
    }

    private Assertion decryptAssertion(EncryptedAssertion encryptedAssertion) {
        try {
            return decrypter.decrypt(encryptedAssertion);
        } catch (DecryptionException e) {
            throw new ResponseAssertionDecryptionException(e);
        }
    }
}
