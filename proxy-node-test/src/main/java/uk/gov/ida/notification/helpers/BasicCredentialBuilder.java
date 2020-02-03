package uk.gov.ida.notification.helpers;

import org.bouncycastle.util.Strings;
import org.glassfish.jersey.internal.util.Base64;
import org.opensaml.security.credential.BasicCredential;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;

public class BasicCredentialBuilder {

    private static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----\n";
    private static final String END_CERT = "\n-----END CERTIFICATE-----";

    private String publicSigningCert;
    private String privateSigningKey;

    private BasicCredentialBuilder() {
    }

    public static BasicCredentialBuilder instance() {
        return new BasicCredentialBuilder();
    }

    public BasicCredentialBuilder withPublicSigningCert(String publicSigningCert) {
        this.publicSigningCert = publicSigningCert;
        return this;
    }

    public BasicCredentialBuilder withPrivateSigningKey(String privateSigningKey) {
        this.privateSigningKey = privateSigningKey;
        return this;
    } 

    public BasicCredential build() throws Exception {
        String publicCert = BEGIN_CERT + publicSigningCert + END_CERT;
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(publicCert.getBytes(StandardCharsets.UTF_8));
        X509Certificate x509certificate = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(byteArrayInputStream);
        PublicKey publicKey = x509certificate.getPublicKey();
        PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.decode(Strings.toByteArray(privateSigningKey))));
        return new BasicCredential(publicKey, privateKey);
    }

}
