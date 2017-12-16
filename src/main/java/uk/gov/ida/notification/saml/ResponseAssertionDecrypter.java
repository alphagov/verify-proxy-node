package uk.gov.ida.notification.saml;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.encryption.Decrypter;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.encryption.support.DecryptionException;
import org.opensaml.xmlsec.encryption.support.InlineEncryptedKeyResolver;
import org.opensaml.xmlsec.keyinfo.impl.StaticKeyInfoCredentialResolver;
import uk.gov.ida.notification.exceptions.ResponseAssertionDecryptionException;

import java.util.stream.Collectors;

public class ResponseAssertionDecrypter {
    private final Decrypter decrypter;

    public ResponseAssertionDecrypter(Credential credential) {
        decrypter = new Decrypter(null, new StaticKeyInfoCredentialResolver(credential), new InlineEncryptedKeyResolver());
        decrypter.setRootInNewDocument(true);
    }

    public Response decrypt(Response encryptedResponse) {
        encryptedResponse.getAssertions().clear();
        encryptedResponse.getAssertions().addAll(encryptedResponse.getEncryptedAssertions()
            .stream()
            .map(a -> decryptAssertion(a))
            .collect(Collectors.toList())
        );

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
