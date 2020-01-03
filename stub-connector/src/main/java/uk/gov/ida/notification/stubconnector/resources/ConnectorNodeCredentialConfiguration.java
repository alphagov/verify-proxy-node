package uk.gov.ida.notification.stubconnector.resources;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.security.x509.X509Credential;
import org.opensaml.security.x509.X509Support;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import uk.gov.ida.common.shared.configuration.DeserializablePublicKeyConfiguration;
import uk.gov.ida.common.shared.configuration.PrivateKeyConfiguration;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;

public class ConnectorNodeCredentialConfiguration {

    private final X509Credential metadataSigningCredential;
    private final X509Credential samlSigningCredential;
    private final X509Credential samlEncryptionCredential;
    private final String algorithm;

    @JsonCreator
    public ConnectorNodeCredentialConfiguration(
            @JsonProperty("metadataSigningPublicKey") DeserializablePublicKeyConfiguration metadataSigningPublicKey,
            @JsonProperty("samlSigningPublicKey") DeserializablePublicKeyConfiguration samlSigningPublicKey,
            @JsonProperty("samlEncryptionPublicKey") DeserializablePublicKeyConfiguration samlEncryptionPublicKey,
            @JsonProperty("metadataSigningPrivateKey") PrivateKeyConfiguration metadataSigningPrivateKey,
            @JsonProperty("samlSigningPrivateKey") PrivateKeyConfiguration samlSigningPrivateKey,
            @JsonProperty("samlEncryptionPrivateKey") PrivateKeyConfiguration samlEncryptionPrivateKey
    ) throws CertificateException {
        this.metadataSigningCredential = new BasicX509Credential(
                getX509Certificate(metadataSigningPublicKey),
                metadataSigningPrivateKey.getPrivateKey());
        this.samlSigningCredential = new BasicX509Credential(
                getX509Certificate(samlSigningPublicKey),
                samlSigningPrivateKey.getPrivateKey());
        this.samlEncryptionCredential = new BasicX509Credential(
                getX509Certificate(samlEncryptionPublicKey),
                samlEncryptionPrivateKey.getPrivateKey());

        if (samlSigningCredential.getPublicKey() instanceof ECPublicKey) {
            this.algorithm = SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA256;
        } else {
            this.algorithm = SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256;
        }

    }

    private X509Certificate getX509Certificate(DeserializablePublicKeyConfiguration metadataSigningPublicKey) throws CertificateException {
        return X509Support.decodeCertificate(metadataSigningPublicKey.getCert().getBytes());
    }

    public Credential getMetadataSigningCredential() {
        return metadataSigningCredential;
    }

    public Credential getSamlSigningCredential() {
        return samlSigningCredential;
    }

    public Credential getSamlEncryptionCredential() {
        return samlEncryptionCredential;
    }

    public String getAlgorithm() {
        return algorithm;
    }
}
