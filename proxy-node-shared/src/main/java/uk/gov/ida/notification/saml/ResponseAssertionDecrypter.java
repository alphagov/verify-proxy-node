package uk.gov.ida.notification.saml;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.encryption.support.DecryptionException;
import se.litsec.opensaml.xmlsec.SAMLObjectDecrypter;

public class ResponseAssertionDecrypter {
    private final SAMLObjectDecrypter decrypter;

    public ResponseAssertionDecrypter(Credential credential) {
        this.decrypter = new SAMLObjectDecrypter(credential);
    }

    public Response decrypt(Response response) throws DecryptionException {
        response.getAssertions().clear();
        for (EncryptedAssertion encryptedAssertion : response.getEncryptedAssertions()) {
            Assertion assertion = decrypter.decrypt(encryptedAssertion, Assertion.class);
            response.getAssertions().add(assertion);
        }
        response.getEncryptedAssertions().clear();
        return response;
    }
}
