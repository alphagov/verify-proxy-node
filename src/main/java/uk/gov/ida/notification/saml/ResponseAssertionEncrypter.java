package uk.gov.ida.notification.saml;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.encryption.Encrypter;
import org.opensaml.xmlsec.encryption.support.EncryptionException;
import uk.gov.ida.notification.exceptions.ResponseAssertionEncryptionException;
import uk.gov.ida.notification.pki.EncryptionCredential;
import uk.gov.ida.saml.security.EncrypterFactory;

import java.util.stream.Collectors;

public class ResponseAssertionEncrypter {
    private final Encrypter encrypter;

    public ResponseAssertionEncrypter(EncryptionCredential credential) {
        EncrypterFactory encrypterFactory = new EncrypterFactory();
        encrypter = encrypterFactory.createEncrypter(credential);
    }

    public Response encrypt(Response response) {
        response.getEncryptedAssertions().clear();
        response.getEncryptedAssertions().addAll(response.getAssertions()
                .stream()
                .map(this::encryptAssertion)
                .collect(Collectors.toList())
        );
        response.getAssertions().clear();

        return response;
    }

    private EncryptedAssertion encryptAssertion(Assertion plaintextAssertion) {
        try {
            return encrypter.encrypt(plaintextAssertion);
        } catch (EncryptionException e) {
            throw new ResponseAssertionEncryptionException(e);
        }
    }
}
