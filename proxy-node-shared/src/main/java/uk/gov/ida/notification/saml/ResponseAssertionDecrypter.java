package uk.gov.ida.notification.saml;

import com.google.common.collect.ImmutableSet;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.encryption.Decrypter;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.encryption.support.EncryptionConstants;
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.saml.security.DecrypterFactory;
import uk.gov.ida.saml.security.validators.ValidatedResponse;
import uk.gov.ida.saml.security.validators.encryptedelementtype.EncryptionAlgorithmValidator;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ResponseAssertionDecrypter {
    private final AssertionDecrypter assertionDecrypter;

    public ResponseAssertionDecrypter(Credential credential) {
        Decrypter decrypter = new DecrypterFactory().createDecrypter(Collections.singletonList(credential));
        decrypter.setRootInNewDocument(true);

        Set<String> contentEncryptionAlgorithms = ImmutableSet.of(
                EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128,
                EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128_GCM,
                EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES256_GCM);

        Set<String> keyTransportAlgorithms = ImmutableSet.of(
                EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP,
                EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP11);

        EncryptionAlgorithmValidator algorithmValidator = new EncryptionAlgorithmValidator(contentEncryptionAlgorithms, keyTransportAlgorithms);

        this.assertionDecrypter = new AssertionDecrypter(algorithmValidator, decrypter);
    }

    public Response decrypt(Response response) {
        ValidatedResponse validatedResponse = new ValidatedResponse(response);
        List<Assertion> assertions = assertionDecrypter.decryptAssertions(validatedResponse);

        response.getAssertions().clear();
        response.getAssertions().addAll(assertions);
        response.getEncryptedAssertions().clear();

        return response;
    }

    public AssertionDecrypter getAssertionDecrypter() {
        return assertionDecrypter;
    }
}
