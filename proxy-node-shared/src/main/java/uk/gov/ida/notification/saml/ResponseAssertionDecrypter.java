package uk.gov.ida.notification.saml;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.encryption.Decrypter;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.encryption.support.DecryptionException;
import uk.gov.ida.notification.configuration.CloudHsmCredentialConfiguration;
import uk.gov.ida.notification.shared.ProxyNodeLogger;
import uk.gov.ida.saml.security.DecrypterFactory;

import java.util.Collections;

public class ResponseAssertionDecrypter {

    private final Decrypter decrypter;

    public ResponseAssertionDecrypter(Credential credential) {
        this.decrypter = new DecrypterFactory().createDecrypter(Collections.singletonList(credential));
        if (credential.getEntityId() != null && credential.getEntityId().equals(CloudHsmCredentialConfiguration.ID)) {
            ProxyNodeLogger.info("Using CloudHSM so set JCA provider to Cavium");
            this.decrypter.setJCAProviderName("Cavium");
        }
    }

    public Response decrypt(Response response) throws DecryptionException {
        response.getAssertions().clear();
        for (EncryptedAssertion encryptedAssertion : response.getEncryptedAssertions()) {
            Assertion assertion = decrypter.decrypt(encryptedAssertion);
            response.getAssertions().add(assertion);
        }
        response.getEncryptedAssertions().clear();
        return response;
    }
}
