package uk.gov.ida.notification.saml;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.encryption.Encrypter;
import org.opensaml.security.SecurityException;
import org.opensaml.security.x509.X509Credential;
import org.opensaml.xmlsec.encryption.support.EncryptionConstants;
import org.opensaml.xmlsec.encryption.support.EncryptionException;
import org.opensaml.xmlsec.keyinfo.KeyInfoSupport;
import org.opensaml.xmlsec.keyinfo.impl.X509KeyInfoGeneratorFactory;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.KeyValue;
import uk.gov.ida.notification.exceptions.hubresponse.ResponseAssertionEncryptionException;
import uk.gov.ida.saml.security.EncrypterFactory;

import java.security.interfaces.RSAPublicKey;
import java.util.stream.Collectors;

public class ResponseAssertionEncrypter {
    private final Encrypter encrypter;
    private X509Credential encryptionCredential;

    public ResponseAssertionEncrypter(X509Credential encryptionCredential) {
        EncrypterFactory encrypterFactory = new EncrypterFactory()
                .withDataEncryptionAlgorithm(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128_GCM)
                .withKeyPlacement(Encrypter.KeyPlacement.INLINE);
        this.encryptionCredential = encryptionCredential;
        this.encrypter = encrypterFactory.createEncrypter(this.encryptionCredential);
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
            EncryptedAssertion encryptedAssertion = encrypter.encrypt(plaintextAssertion);
            encryptedAssertion.getEncryptedData().getKeyInfo().getEncryptedKeys().get(0).setKeyInfo(buildKeyInfo());
            return encryptedAssertion;
        } catch (EncryptionException | SecurityException e) {
            throw new ResponseAssertionEncryptionException(e);
        }
    }

    private KeyInfo buildKeyInfo() throws SecurityException {
        X509KeyInfoGeneratorFactory x509KeyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
        x509KeyInfoGeneratorFactory.setEmitEntityCertificate(true);
        KeyInfo keyInfo = x509KeyInfoGeneratorFactory.newInstance().generate(encryptionCredential);

        KeyValue keyValue = SamlBuilder.build(KeyValue.DEFAULT_ELEMENT_NAME);
        RSAPublicKey publicKey = (RSAPublicKey) encryptionCredential.getPublicKey();
        keyValue.setRSAKeyValue(KeyInfoSupport.buildRSAKeyValue(publicKey));

        keyInfo.getKeyValues().add(keyValue);

        return keyInfo;
    }
}
