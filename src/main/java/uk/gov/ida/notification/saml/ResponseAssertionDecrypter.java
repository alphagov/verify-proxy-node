package uk.gov.ida.notification.saml;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.encryption.Decrypter;
import org.opensaml.xmlsec.encryption.support.DecryptionException;
import uk.gov.ida.notification.exceptions.ResponseAssertionDecryptionException;
import uk.gov.ida.notification.pki.DecryptionCredential;
import uk.gov.ida.saml.security.DecrypterFactory;

import java.util.Collections;
import java.util.stream.Collectors;

public class ResponseAssertionDecrypter {
    private final Decrypter decrypter;

    public ResponseAssertionDecrypter(DecryptionCredential credential) {
        DecrypterFactory decrypterFactory = new DecrypterFactory();
        decrypter = decrypterFactory.createDecrypter(Collections.singletonList(credential));
        decrypter.setRootInNewDocument(true);
    }

    public Response decrypt(Response response) {
        response.getAssertions().clear();
        response.getAssertions().addAll(response.getEncryptedAssertions()
            .stream()
            .map(this::decryptAssertion)
            .collect(Collectors.toList())
        );
        response.getEncryptedAssertions().clear();

        return response;
    }

    private Assertion decryptAssertion(EncryptedAssertion encryptedAssertion) {
        try {
            return decrypter.decrypt(encryptedAssertion);
        } catch (DecryptionException e) {
            throw new ResponseAssertionDecryptionException(e);
        }
    }
}
