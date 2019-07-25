package uk.gov.ida.notification.helpers;

import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509ExtensionUtils;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import javax.security.auth.x500.X500Principal;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

public class SelfSignedCertificateGenerator {

    private String cn;
    private KeyPair keys;
    private X509Certificate certificate;

    public SelfSignedCertificateGenerator(String cn) {
        this.cn = cn;
    }

    public String getCN() { return cn; }

    public KeyPair getKeys() throws NoSuchAlgorithmException {
        if (keys == null) {
            keys = generateKeys("RSA");
        }
        return keys;
    }

    public X509Certificate getCertificate() throws NoSuchAlgorithmException, CertificateException, CertIOException, OperatorCreationException {
        if (certificate == null) {
            certificate = generateCert_v3(getKeys(), "SHA256withRSA", getCN(), 1);
        }
        return certificate;
    }

    public String getCertificateAsPEM() throws OperatorCreationException, CertificateException, CertIOException, NoSuchAlgorithmException {
        return convertToPem(getCertificate());
    }

    private KeyPair generateKeys(String algorithm) throws NoSuchAlgorithmException {
        KeyPairGenerator gen = KeyPairGenerator.getInstance(algorithm);
        return gen.generateKeyPair();
    }

    private X509Certificate generateCert_v3(KeyPair keyPair, String hashAlgorithm, String cn, int days)
            throws CertificateException, OperatorCreationException, CertIOException {

        Instant now = Instant.now();
        Date notBefore = Date.from(now);
        Date notAfter = Date.from(now.plus(Duration.ofDays(days)));

        X500Principal x500Name = new X500Principal("CN=" + cn);
        X509v3CertificateBuilder certificateBuilder =
            new JcaX509v3CertificateBuilder(
                x500Name,
                BigInteger.valueOf(now.toEpochMilli()),
                notBefore,
                notAfter,
                x500Name,
                keyPair.getPublic())
                .addExtension(Extension.subjectKeyIdentifier, false, createSubjectKeyId(keyPair.getPublic()))
                .addExtension(Extension.authorityKeyIdentifier, false, createAuthorityKeyId(keyPair.getPublic()))
                .addExtension(Extension.basicConstraints, true, new BasicConstraints(true));

        ContentSigner signer = new JcaContentSignerBuilder(hashAlgorithm).build(keyPair.getPrivate());
        X509CertificateHolder holder = certificateBuilder.build(signer);

        return new JcaX509CertificateConverter()
            .setProvider(new BouncyCastleProvider())
            .getCertificate(holder);
    }

    private static SubjectKeyIdentifier createSubjectKeyId(final PublicKey publicKey) throws OperatorCreationException {
        final SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(publicKey.getEncoded());
        final DigestCalculator digCalc =
            new BcDigestCalculatorProvider().get(new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1));

        return new X509ExtensionUtils(digCalc).createSubjectKeyIdentifier(publicKeyInfo);
    }

    private static AuthorityKeyIdentifier createAuthorityKeyId(final PublicKey publicKey) throws OperatorCreationException {
        final SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(publicKey.getEncoded());
        final DigestCalculator digCalc =
            new BcDigestCalculatorProvider().get(new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1));

        return new X509ExtensionUtils(digCalc).createAuthorityKeyIdentifier(publicKeyInfo);
    }

    public static String convertToPem(X509Certificate cert) throws CertificateEncodingException {
        Base64.Encoder encoder = Base64.getMimeEncoder(64, new byte[] { '\n' });
        String cert_begin = "-----BEGIN CERTIFICATE-----\n";
        String end_cert = "-----END CERTIFICATE-----";
        byte[] derCert = cert.getEncoded();
        String pemCertPre = new String(encoder.encode(derCert));
        return cert_begin + pemCertPre + end_cert;
    }
}
